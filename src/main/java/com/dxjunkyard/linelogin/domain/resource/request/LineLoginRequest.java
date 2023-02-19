package org.microvolunteer.platform.domain.resource.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LineLoginRequest {
    private String response_type;
    private String redirect_uri;
    private String client_id;
}
