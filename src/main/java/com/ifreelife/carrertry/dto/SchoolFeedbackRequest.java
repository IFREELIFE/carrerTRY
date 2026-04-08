package com.ifreelife.carrertry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolFeedbackRequest {

    @NotBlank
    private String studentName;

    @NotBlank
    private String mentor;

    @NotBlank
    private String comment;
}
