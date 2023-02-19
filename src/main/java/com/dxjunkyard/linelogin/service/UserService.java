package com.dxjunkyard.linelogin.service;


import com.dxjunkyard.linelogin.api.client.LineLoginRestClient;
import com.dxjunkyard.linelogin.controller.Controller;
import com.dxjunkyard.linelogin.domain.resource.*;
import com.dxjunkyard.linelogin.repository.dao.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LineLoginRestClient lineLoginRestClient;

    @Value("${encrypt.volunteerdb.key}")
    private String encrypt_key;

    /*
     * ユーザーの新規作成（ユーザー情報は空）
     * 1 user_idの作成
     * 2 x,yの初期座標(mygeometry)作成
     * 3 user_id & MyGeometryの登録
     */
    public String createUser() {
        UUID uuid = UUID.randomUUID();
        userMapper.registerUserProperty(
                RegisterUserProperty.builder()
                        .encrypt_key(encrypt_key)
                        .user_id(uuid.toString())
                        .email("")
                        .password("password")
                        .build()
        );
        return uuid.toString();
    }

    /*
     *
     */
    public void registerUserInfo(
            String user_id,
            String user_name,
            String user_email,
            String user_password) {
        userMapper.updateUserProperty(
                RegisterUserProperty.builder()
                        .encrypt_key(encrypt_key)
                        .user_id(user_id)
                        .name(user_name)
                        .email(user_email)
                        .password(user_password)
                        .status(1)
                        .build());
    }

    /*
     *
     */
    public String login(
            String email,
            String password) {
        String user_id = userMapper.login(
                Login.builder()
                        .encrypt_key(encrypt_key)
                        .email(email)
                        .password(password)
                        .build());
        return user_id;
    }





    public String lineAuth(String code) {
        String id_token = lineLoginRestClient.lineAuth(code);
        return id_token;
    }

    public void lineLogin() {
        lineLoginRestClient.lineLogin();
    }
}
