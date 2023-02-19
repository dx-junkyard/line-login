package com.dxjunkyard.linelogin.domain.resource.request;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class LoginRequest {
    @Email
    private String email;

    private String password;
}
