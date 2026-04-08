package com.ifreelife.carrertry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class JobCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String enterpriseName;

    @NotBlank
    private String department;

    @NotBlank
    private String location;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal salaryMin;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal salaryMax;

    @NotBlank
    private String experienceRequirement;

    @NotBlank
    private String educationRequirement;

    @NotBlank
    private String skills;
}
