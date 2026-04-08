package com.ifreelife.carrertry.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterEnterpriseRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String displayName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String enterpriseName;

    @NotBlank
    private String unifiedSocialCreditCode;

    @NotBlank
    private String legalRepresentative;
}
