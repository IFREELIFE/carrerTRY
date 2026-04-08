package com.ifreelife.carrertry.dto;

import com.ifreelife.carrertry.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {
    private String username;
    private String displayName;
    private String email;
    private UserRole role;
}
