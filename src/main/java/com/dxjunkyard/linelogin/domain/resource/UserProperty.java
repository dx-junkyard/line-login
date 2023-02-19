package com.dxjunkyard.linelogin.domain.resource;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProperty {
    private String user_id;
    private String name;
    private String email;
}
