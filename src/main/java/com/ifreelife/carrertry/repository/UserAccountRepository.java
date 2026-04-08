package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
