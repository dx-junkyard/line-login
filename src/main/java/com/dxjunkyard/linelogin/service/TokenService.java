package com.dxjunkyard.linelogin.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dxjunkyard.linelogin.repository.dao.mapper.SnsRegisterMapper;
import com.dxjunkyard.linelogin.repository.dao.mapper.TokenMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {
    private Logger logger = LoggerFactory.getLogger(com.dxjunkyard.linelogin.service.TokenService.class);

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private SnsRegisterMapper snsRegisterMapper;

    @Value("${line-login.client_secret}")
    private String client_secret;

    @Value("${encrypt.jwt.secret}")
    private String jwt_secret;

    @Value("${encrypt.jwt.expire}")
    private Integer jwt_expire;

    public String createToken(String user_id) {
        String token = null;
        try {
            Date expDate = new Date();
            expDate.setTime(expDate.getTime() + jwt_expire);

            Algorithm algorithm = Algorithm.HMAC256(jwt_secret);
            token = JWT.create()
                    .withIssuer("auth0")
                    .withExpiresAt(expDate)
                    .withClaim("user_id",user_id)
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            logger.error("error : " + exception.getMessage());
            return null;
            //無効なトークンの場合:メッセージは変更する。
        }
        return token;
    }

    public String getUserId(String token) {
        String user_id = null;
        try {
            // 期限&改竄チェック
            Algorithm algorithm = Algorithm.HMAC256(jwt_secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            // decodeしてuser_idを取得する
            Claim claim = jwt.getClaim("user_id");
            user_id = claim.asString();
        } catch (JWTDecodeException exception){
            logger.error("Invalid token");
            throw exception;
            //Invalid token
            //return null;
        } catch (TokenExpiredException exception) {
            logger.error("Token expired");
            throw exception;
            //return null;
        }
        return user_id;
    }

    public String getTokenByUserId(String user_id) {
        return this.createToken(user_id);
        //return tokenMapper.getTokenByUserId(user_id);
    }

    public String getSnsIdFromLineToken(String idToken) {
        String user_id = null;
        try {
            // 期限&改竄チェック
            Algorithm algorithm = Algorithm.HMAC256(client_secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("https://access.line.me")
                    .build();
            DecodedJWT jwt = verifier.verify(idToken);
            // decodeしてuser_idを取得する
            String sub = jwt.getClaim("sub").asString();
            String name = jwt.getClaim("name").asString();
            //String exp = jwt.getClaim("exp").asString();
            user_id = sub;
        } catch (JWTDecodeException exception){
            logger.error("Invalid token");
            throw exception;
            //Invalid token
            //return null;
        } catch (TokenExpiredException exception) {
            logger.error("Token expired");
            throw exception;
            //return null;
        }
        return user_id;
    }

    public String getUserIdBySnsId(String sns_id) {
        return snsRegisterMapper.getUserId(sns_id);
    }

    public String getTokenFromAuth(String auth) {
        String[] split = auth.split(" ");
        if (split.length != 2) {
            return null;
        }
        return split[0].equals("Bearer") ? split[1] : null;
    }
}
