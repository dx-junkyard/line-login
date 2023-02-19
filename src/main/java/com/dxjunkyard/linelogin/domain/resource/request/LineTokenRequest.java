package org.microvolunteer.platform.domain.resource.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LineTokenRequest {
    private String grant_type;
    private String code;
    private String redirect_uri;
    private String client_id;
    private String client_secret;
}
