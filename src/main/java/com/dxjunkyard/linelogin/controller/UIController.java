package com.dxjunkyard.linelogin.controller;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.dxjunkyard.linelogin.domain.resource.*;
import com.dxjunkyard.linelogin.domain.resource.request.*;
import com.dxjunkyard.linelogin.domain.resource.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@CrossOrigin
@Controller
@RequestMapping("/v1")
public class UIController {
    private Logger logger = LoggerFactory.getLogger(com.dxjunkyard.linelogin.controller.Controller.class);

    @Value("${backend-api.uri}")
    private String api_uri;

    @Value("${line-login.client_id}")
    private String client_id;

    @Value("${line-login.client_secret}")
    private String client_secret;

    @Value("${line-login.login_redirect_uri}")
    private String login_redirect_uri;

    @GetMapping("/user/line-login")
    @ResponseBody
    public void linelogin(HttpServletResponse httpServletResponse) {
        logger.info("line login API");
        String redirect_url = "https://access.line.me/oauth2/v2.1/authorize?response_type=code&client_id=" + client_id + "&redirect_uri=" + login_redirect_uri + "&state=1&scope=openid%20profile";
        httpServletResponse.setHeader("Location", redirect_url);
        httpServletResponse.setStatus(302);
    }

    /**
     * LINE Auth
     */
    @GetMapping("/auth")
    public String line_auth(HttpServletResponse response, @RequestParam("code") String code, Model model){
        logger.info("LINE Auth API");
        String api_url = api_uri + "/v1/api/auth?code=" + code;
        logger.info("sns register API");
        String lineId = ""; // LINE user ID
        /*
         * LINE APIを使用してLINE user IDを取得する。
         */
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<LoginResponse> registerResponse = restTemplate
                    .exchange(api_url, HttpMethod.GET, null, LoginResponse.class);
            // get LINE user ID
            lineId = registerResponse.getBody().getToken();
            if (lineId.isEmpty()) throw new RestClientException("get lineId error.");
        } catch (RestClientException e) {
            logger.info("RestClient error : {}", e.toString());
            return "error"; // error page遷移
        }


        /*
         * このシステムへの登録を行う
         */
        api_url = api_uri + "/v1/api/user/register/" + lineId;
        logger.info("sns register API");
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<SnsTokenResponse> registerResponse = restTemplate
                    .exchange(api_url, HttpMethod.GET, null, SnsTokenResponse.class);
            String token = registerResponse.getBody().getToken();
            if (token.isEmpty()) throw new RestClientException("get token error.");

            // cookieを設定
            Cookie cookie = new Cookie("_token",token);
            cookie.setPath("/");
            response.addCookie(cookie);

            // modelに変数を設定
            RegisterUserRequest registerUserRequest = new RegisterUserRequest();
            model.addAttribute(registerUserRequest);
            return "user_registration";
        } catch (RestClientException e) {
            logger.info("RestClient error : {}", e.toString());
            return "error"; // error page遷移
        }
    }

    /*
     * ログイン画面の表示
     */
    @GetMapping("/user/login")
    public String login(@CookieValue(value="_token", required=false) String token, Model model) {
        logger.info("login");
        if (token != null && tokenCheck(token)) {
            // 有効なtokenがある場合は、profile画面を表示
            try {
                //MyProfile myProfile = getMyProfile(token);
                //model.addAttribute(myProfile);
                //HelpRequest helpRequest = new HelpRequest();
                //model.addAttribute(helpRequest);
                return "my_profile";
            } catch (Exception e) {
                logger.info("error : {}", e.toString());
                return "error"; // error page遷移
            }
        } else {
            model.addAttribute(new LoginRequest());
            return "login_form";
        }
    }

    /**
     * ログイン後、mypageを表示
     * @param loginRequest
     * @return
     */
    @PostMapping("/user/mypage")
    public String default_login(HttpServletResponse response, LoginRequest loginRequest, Model model){
        logger.info("ログインAPI");
        // api に置き換え
        try {
            // login & token取得
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LoginRequest> loginRequestEntity = new HttpEntity<>(loginRequest,headers);
            RestTemplate loginRestTemplate = new RestTemplate();
            ResponseEntity<LoginResponse> loginResponse = loginRestTemplate
                    .exchange(api_uri + "/v1/api/user/login", HttpMethod.POST, loginRequestEntity, LoginResponse.class);
            String token = loginResponse.getBody().getToken();
            if (token == null) {
                model.addAttribute("loginRequest", new LoginRequest());
                return "login_form";
            }

            // cookieを設定
            Cookie cookie = new Cookie("_token",token);
            cookie.setPath("/");
            response.addCookie(cookie);

            //MyProfile myProfile = getMyProfile(token);
            // model 設定
            //model.addAttribute(myProfile);
            return "my_profile";
        } catch (Exception e) {
            logger.info("error : {}", e.toString());
            return "error"; // error page遷移
        }
    }

    /**
     * myprofile画面の表示
     * @param token
     * @return
     */
    @GetMapping("/user/mypage")
    public String mypage(@CookieValue(value="_token", required=true) String token, Model model){
        logger.info("mypage API");
        try {
            //MyProfile myProfile = getMyProfile(token);
            //model.addAttribute(myProfile);
            // model.addAttribute(xxx);
            return "my_profile";
        } catch (RestClientException e) {
            logger.info("RestClient error : {}", e.toString());
            return "error"; // error page遷移
        } catch (JWTDecodeException | TokenExpiredException e) {
            logger.error("JWT decode failed or expired.");
            model.addAttribute(new LoginRequest());
            return "login_form";
        } catch (Exception e) {
            logger.info("error : {}", e.toString());
            return "error"; // error page遷移
        }
    }

    /*
     * tokenの有効性を確認する
     */
    private Boolean tokenCheck(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<NormalResponse> response = restTemplate
                    .exchange(api_uri + "/v1/api/user/tokencheck", HttpMethod.GET, requestEntity, NormalResponse.class);
            if ("NG" == response.getBody().getResult()) {
                return false;
            } else {
                return true;
            }
        } catch (RestClientException e) {
            // error message log
            return false;
        } catch (Exception e) {
            // error message log
            //unexpected error
            return false;
        }
    }


    /*
     * ユーザー登録
     */
    @PostMapping("/user/register")
    public String default_register(@CookieValue(value="_token", required=true) String token, RegisterUserRequest registerUserRequest,Model model){
        logger.info("default register API");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<RegisterUserRequest> requestEntity = new HttpEntity<>(registerUserRequest,headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<RegisterUserResponse> response = restTemplate
                    .exchange(api_uri + "/v1/api/user/register", HttpMethod.POST, requestEntity, RegisterUserResponse.class);
            //String result = response.getBody().getResult();
            // ログイン画面から登録したemail & passwordでログインしてもらう
            model.addAttribute("loginRequest", new LoginRequest());
            return "login_form";
        } catch (Exception e) {
            // invalid token, require login
            model.addAttribute(new LoginRequest());
            return "login_form";
        }
    }



}
