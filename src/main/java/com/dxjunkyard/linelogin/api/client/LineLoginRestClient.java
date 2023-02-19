package com.dxjunkyard.linelogin.api.client;

import com.dxjunkyard.linelogin.domain.resource.response.LineTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;

@Component
public class LineLoginRestClient {
    private Logger logger = LoggerFactory.getLogger(LineLoginRestClient.class);

    @Value("${line-login.client_id}")
    private String client_id;

    @Value("${line-login.client_secret}")
    private String client_secret;

    @Value("${line-login.line_token_url}")
    private String line_token_url;

    @Value("${line-login.line_login_url}")
    private String line_login_url;

    @Value("${line-login.login_redirect_uri}")
    private String login_redirect_uri;

    public String lineAuth(String code) {
        RestTemplate restTemplate = new RestTemplate();


        MultiValueMap<String,String> map = new LinkedMultiValueMap<>();
        map.add("grant_type","authorization_code");
        map.add("code",code);
        map.add("client_id",client_id);
        map.add("client_secret",client_secret);
        map.add("redirect_uri",login_redirect_uri);

        try {
            RequestEntity request = RequestEntity
                    .post(URI.create(line_token_url))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(map);
            ResponseEntity<LineTokenResponse> response = restTemplate.exchange(request, LineTokenResponse.class);
            String id_token = response.getBody().getId_token();
            return id_token;

        } catch (RestClientException e) {
            logger.info("RestClient error : {}", e.toString());
            return "";
        }
    }

    public void lineLogin() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String response_type = "code";
            String state="191001000";
            String scope= "openid%20profile";
            ResponseEntity<String> response = restTemplate.exchange(line_login_url,
                    HttpMethod.GET,
                    null,
                    String.class,
                    response_type,client_id,login_redirect_uri,response_type,state,scope);
            logger.info("RestClient response : {}", response);
        } catch (RestClientException e) {
            logger.info("RestClient error : {}", e.toString());
        }
    }
}
