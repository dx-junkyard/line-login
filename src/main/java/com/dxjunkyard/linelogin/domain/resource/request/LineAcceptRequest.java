package org.microvolunteer.platform.domain.resource.request;

import lombok.Data;

@Data
public class LineAcceptRequest {
    private String sns_id;
    private Integer help_id;
}
