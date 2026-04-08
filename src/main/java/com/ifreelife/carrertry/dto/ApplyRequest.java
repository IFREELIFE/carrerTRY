package com.ifreelife.carrertry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyRequest {

    @NotNull
    private Long jobId;

    @NotBlank
    private String resumeSummary;
}
