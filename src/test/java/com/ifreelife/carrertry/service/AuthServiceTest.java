package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.RegisterEnterpriseRequest;
import com.ifreelife.carrertry.dto.RegisterSchoolRequest;
import com.ifreelife.carrertry.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EnterpriseVerificationService enterpriseVerificationService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerSchoolShouldRejectNonEduEmail() {
        RegisterSchoolRequest request = new RegisterSchoolRequest();
        request.setUsername("schoolUser");
        request.setPassword("password");
        request.setDisplayName("School Admin");
        request.setSchoolName("Career School");
        request.setEmail("admin@gmail.com");

        when(userAccountRepository.existsByUsername("schoolUser")).thenReturn(false);
        when(userAccountRepository.existsByEmail("admin@gmail.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.registerSchool(request));
        verify(userAccountRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registerEnterpriseShouldRejectWhenVerificationFailed() {
        RegisterEnterpriseRequest request = new RegisterEnterpriseRequest();
        request.setUsername("enterpriseUser");
        request.setPassword("password");
        request.setDisplayName("Enterprise Admin");
        request.setEmail("admin@company.com");
        request.setEnterpriseName("Career Co");
        request.setUnifiedSocialCreditCode("123456789");
        request.setLegalRepresentative("张三");

        when(userAccountRepository.existsByUsername("enterpriseUser")).thenReturn(false);
        when(userAccountRepository.existsByEmail("admin@company.com")).thenReturn(false);
        when(enterpriseVerificationService.verify("Career Co", "123456789", "张三")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.registerEnterprise(request));
        verify(userAccountRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
