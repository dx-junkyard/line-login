package org.microvolunteer.platform.domain.resource.request;

import lombok.Data;

@Data
public class LineMessageRequest {
    private String sns_id;
    private String message;
}
