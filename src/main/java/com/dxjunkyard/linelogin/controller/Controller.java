package com.dxjunkyard.linelogin.controller;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import com.dxjunkyard.linelogin.domain.resource.*;
import com.dxjunkyard.linelogin.domain.resource.request.*;
import com.dxjunkyard.linelogin.domain.resource.response.*;
import com.dxjunkyard.linelogin.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/api")
@Slf4j
public class Controller {
    private Logger logger = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SnsIdRegisterService snsIdRegisterService;

    /**
     * LINE ログイン
     */
    @GetMapping("/user/line-login")
    @ResponseBody
    public String line_login() {
        //userService.lineLogin();
        String redirect = "redirect:" + "https://access.line.me/oauth2/v2.1/authorize?response_type=code&client_id=1657460430&redirect_uri=http://127.0.0.1:8080/v1/api/auth&state=1&scope=openid%20profile";
        return redirect;
    }

    /**
     * LINE Auth
     */
    @GetMapping("/auth")
    @ResponseBody
    public LoginResponse line_auth(@RequestParam("code") String code){
        logger.info("LINE Auth API");
        String lineIdToken= userService.lineAuth(code);
        String userId = tokenService.getSnsIdFromLineToken(lineIdToken);
        return LoginResponse.builder().token(userId).build();
    }

    /**
     * ログインAPI.
     */
    @PostMapping("/user/login")
    @ResponseBody
    @ApiOperation(value="login", notes="emailとpasswordでログインし、tokenを取得する")
    public LoginResponse app_login(@RequestBody LoginRequest loginRequest){
        logger.info("App ログインAPI");
        String user_id = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        String token = tokenService.createToken(user_id);
        return LoginResponse.builder().token(token).build();
    }

    /**
     * [3] /user/sns_register APIでsns_idを登録＆トークン発行
     * ☆新規ユーザー登録(LINE Bot経由：現在の登録手段はこれのみ）
     * 残課題　issue : https://github.com/urashin/micro-volunteer-docs/issues/37
     * 1) SnsIdテーブルにsns_idを追加
     * 2) Usersテーブルにuser_id(create), email(default), password(default), status(init)をinsertする
     *
     */
    @GetMapping("/user/register/{sns_id}")
    @ApiOperation(value="新規ユーザー登録(1) for LINE user", notes="LINEのuser idを用いた新規ユーザーユーザー登録")
    public SnsTokenResponse snsRegister(@PathVariable String sns_id){
        logger.info("sns register API");
        try {
            // 1) user_id を新規発行（個々の情報はパスワード設定など、個別に設定）
            String user_id = userService.createUser();

            // 2) session 管理のトークンを発行
            String token = tokenService.createToken(user_id);

            // 3) SnsId tableにuser_id&sns_idのペアで登録し、紐付け完了
            snsIdRegisterService.registerSnsId(sns_id, user_id);
            return SnsTokenResponse.builder().token(token).result("OK").build();
            // Usersテーブルにuser_id & statusのみinsertする（他の要素はonetimeurl発行→登録
        } catch (Exception e) {
            return SnsTokenResponse.builder().result("NG").build();
        }
    }

    /*
     * sns_idからtokenを取得する
     */
    @GetMapping("/user/token/{sns_id}")
    @ApiOperation(value="既存ユーザーのSNS ID: for LINE user", notes="登録済みLINEのuser idからtokenを生成する")
    public SnsTokenResponse snsToken(@PathVariable String sns_id){
        logger.info("get token API");
        try {
            String user_id = tokenService.getUserIdBySnsId(sns_id);
            String token = tokenService.getTokenByUserId(user_id);
            return SnsTokenResponse.builder().token(token).result("OK").build();
        } catch (Exception e) {
            logger.error("bad sns_id");
            return SnsTokenResponse.builder().result("NG").build();
        }
    }

    @GetMapping("/user/tokencheck")
    @ResponseBody
    @ApiOperation(value="新規ユーザー登録(2) ユーザー情報設定(共通)", notes="新規ユーザー登録(1)で取得したtokenを用いて名前、email、passwordを設定する")
    public NormalResponse token_check(
            @RequestHeader(value="Authorization",required=true) String auth) {
        logger.info("token check API");
        try {
            String user_id = tokenService.getUserId(tokenService.getTokenFromAuth(auth));
            return NormalResponse.builder().result("OK").build();
        } catch (JWTDecodeException | TokenExpiredException e) {
            logger.info("bad token");
            return NormalResponse.builder().result("NG").build();
        }
    }

    /**
     * email, password, nameを登録.
     * sns_idもしくはonetimeurlにより、tokenは取得できている状態
     *
     * @param registerUserRequest
     * @return
     */
    @PostMapping("/user/register")
    @ResponseBody
    @ApiOperation(value="新規ユーザー登録(2) ユーザー情報設定(共通)", notes="新規ユーザー登録(1)で取得したtokenを用いて名前、email、passwordを設定する")
    public RegisterUserResponse app_register(
            @RequestHeader(value="Authorization",required=true) String auth,
            @RequestBody RegisterUserRequest registerUserRequest){
        logger.info("app register API");
        try {
            String user_id = tokenService.getUserId(tokenService.getTokenFromAuth(auth));
            userService.registerUserInfo(
                    user_id,
                    registerUserRequest.getName(),
                    registerUserRequest.getEmail(),
                    registerUserRequest.getPassword());
            return RegisterUserResponse.builder().result("OK").build();
        } catch (JWTDecodeException | TokenExpiredException e) {
            logger.info("bad token");
            return RegisterUserResponse.builder().result("NG").build();
        }
    }
}
