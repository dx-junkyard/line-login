package com.dxjunkyard.linelogin.domain.resource;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserProperty {
    private String encrypt_key;
    private String user_id;
    private String name;
    private String password;
    private String email;
    private Integer status;
}
