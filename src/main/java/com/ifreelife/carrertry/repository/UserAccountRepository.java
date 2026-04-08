package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<UserAccount> findByRole(UserRole role);

    List<UserAccount> findByRoleAndSchoolName(UserRole role, String schoolName);

    Optional<UserAccount> findByRoleAndSchoolNameAndDisplayNameIgnoreCase(UserRole role, String schoolName, String displayName);
}
