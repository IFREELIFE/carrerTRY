package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "user_account",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_account_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_user_account_email", columnNames = "email")
    }
)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 128)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    @Column(nullable = false, length = 128)
    private String displayName;

    @Column(length = 128)
    private String schoolName;

    @Column(length = 128)
    private String enterpriseName;

    @Column(length = 64)
    private String unifiedSocialCreditCode;

    @Column(length = 64)
    private String legalRepresentative;

    @Column(length = 32)
    private String phone;

    @Column(length = 64)
    private String major;

    @Column(nullable = false)
    private Boolean onboardingCompleted = Boolean.FALSE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
