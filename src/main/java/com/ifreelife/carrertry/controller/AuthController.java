package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.dto.RegisterEnterpriseRequest;
import com.ifreelife.carrertry.dto.RegisterResponse;
import com.ifreelife.carrertry.dto.RegisterSchoolRequest;
import com.ifreelife.carrertry.dto.RegisterStudentRequest;
import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/register")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/student")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse registerStudent(@RequestBody @Valid RegisterStudentRequest request) {
        return toResponse(authService.registerStudent(request));
    }

    @PostMapping("/school")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse registerSchool(@RequestBody @Valid RegisterSchoolRequest request) {
        return toResponse(authService.registerSchool(request));
    }

    @PostMapping("/enterprise")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse registerEnterprise(@RequestBody @Valid RegisterEnterpriseRequest request) {
        return toResponse(authService.registerEnterprise(request));
    }

    private RegisterResponse toResponse(UserAccount account) {
        return RegisterResponse.builder()
            .username(account.getUsername())
            .displayName(account.getDisplayName())
            .email(account.getEmail())
            .role(account.getRole())
            .build();
    }
}
