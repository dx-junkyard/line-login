package com.dxjunkyard.linelogin.domain.resource;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Login {
    private String encrypt_key;
    private String email;
    private String password;
}
