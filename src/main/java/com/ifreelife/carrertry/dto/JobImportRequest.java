package com.ifreelife.carrertry.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JobImportRequest {

    @Valid
    @NotEmpty
    private List<JobCreateRequest> jobs;
}
