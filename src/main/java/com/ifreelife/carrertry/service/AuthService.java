package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.RegisterEnterpriseRequest;
import com.ifreelife.carrertry.dto.RegisterSchoolRequest;
import com.ifreelife.carrertry.dto.RegisterStudentRequest;
import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.entity.UserRole;
import com.ifreelife.carrertry.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnterpriseVerificationService enterpriseVerificationService;

    @Transactional
    public UserAccount registerStudent(RegisterStudentRequest request) {
        validateUsernameAndEmail(request.getUsername(), request.getEmail());
        UserAccount account = baseAccount(
            request.getUsername(),
            request.getPassword(),
            request.getEmail(),
            request.getDisplayName(),
            UserRole.STUDENT
        );
        account.setSchoolName(request.getSchoolName());
        return userAccountRepository.save(account);
    }

    @Transactional
    public UserAccount registerSchool(RegisterSchoolRequest request) {
        validateUsernameAndEmail(request.getUsername(), request.getEmail());
        if (!isSchoolEmail(request.getEmail())) {
            throw new IllegalArgumentException("School registration requires .edu or .edu.cn email");
        }
        UserAccount account = baseAccount(
            request.getUsername(),
            request.getPassword(),
            request.getEmail(),
            request.getDisplayName(),
            UserRole.SCHOOL
        );
        account.setSchoolName(request.getSchoolName());
        return userAccountRepository.save(account);
    }

    @Transactional
    public UserAccount registerEnterprise(RegisterEnterpriseRequest request) {
        validateUsernameAndEmail(request.getUsername(), request.getEmail());
        boolean verified = enterpriseVerificationService.verify(
            request.getEnterpriseName(),
            request.getUnifiedSocialCreditCode(),
            request.getLegalRepresentative()
        );
        if (!verified) {
            throw new IllegalArgumentException("Enterprise verification failed");
        }
        UserAccount account = baseAccount(
            request.getUsername(),
            request.getPassword(),
            request.getEmail(),
            request.getDisplayName(),
            UserRole.ENTERPRISE
        );
        account.setEnterpriseName(request.getEnterpriseName());
        account.setUnifiedSocialCreditCode(request.getUnifiedSocialCreditCode());
        account.setLegalRepresentative(request.getLegalRepresentative());
        return userAccountRepository.save(account);
    }

    private UserAccount baseAccount(String username, String password, String email, String displayName, UserRole role) {
        UserAccount account = new UserAccount();
        account.setUsername(username.trim());
        account.setPassword(passwordEncoder.encode(password));
        account.setEmail(email.trim().toLowerCase());
        account.setDisplayName(displayName.trim());
        account.setRole(role);
        return account;
    }

    private void validateUsernameAndEmail(String username, String email) {
        if (userAccountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userAccountRepository.existsByEmail(email.toLowerCase())) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    private boolean isSchoolEmail(String email) {
        String lower = email.toLowerCase();
        return lower.endsWith(".edu") || lower.endsWith(".edu.cn");
    }
}
