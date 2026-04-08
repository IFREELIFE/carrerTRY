package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.entity.UserRole;
import com.ifreelife.carrertry.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAccountBootstrapService implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${RBAC_ENTERPRISE_PASSWORD:Enterprise@1234}")
    private String enterprisePassword;

    @Value("${RBAC_ADMIN_PASSWORD:Admin@1234}")
    private String adminPassword;

    @Value("${RBAC_STUDENT_PASSWORD:Student@1234}")
    private String studentPassword;

    @Value("${RBAC_SCHOOL_PASSWORD:School@1234}")
    private String schoolPassword;

    @Override
    public void run(ApplicationArguments args) {
        createIfMissing("admin", "System Admin", "admin@careertry.local", adminPassword, UserRole.ADMIN);
        createIfMissing("enterprise", "Enterprise Demo", "enterprise@careertry.local", enterprisePassword, UserRole.ENTERPRISE);
        createIfMissing("student", "Student Demo", "student@careertry.local", studentPassword, UserRole.STUDENT);
        createIfMissing("school", "School Demo", "school@careertry.local", schoolPassword, UserRole.SCHOOL);
    }

    private void createIfMissing(String username, String displayName, String email, String rawPassword, UserRole role) {
        if (userAccountRepository.existsByUsername(username)) {
            return;
        }
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setDisplayName(displayName);
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setRole(role);
        if (role == UserRole.SCHOOL || role == UserRole.STUDENT) {
            account.setSchoolName("Demo School");
        }
        if (role == UserRole.ENTERPRISE) {
            account.setEnterpriseName("Demo Enterprise");
            account.setUnifiedSocialCreditCode("91300000DEMO00001");
            account.setLegalRepresentative("Demo Legal");
        }
        userAccountRepository.save(account);
    }
}
