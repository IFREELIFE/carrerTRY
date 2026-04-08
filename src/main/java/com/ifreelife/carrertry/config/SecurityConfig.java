package com.ifreelife.carrertry.config;

import com.ifreelife.carrertry.common.RoleConstants;
import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.repository.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/auth/register/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/register/**").permitAll()
                .requestMatchers("/enterprise/**").hasRole(RoleConstants.ENTERPRISE)
                .requestMatchers("/admin/**").hasRole(RoleConstants.ADMIN)
                .requestMatchers("/student/**").hasRole(RoleConstants.STUDENT)
                .requestMatchers("/school/**").hasRole(RoleConstants.SCHOOL)
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserAccountRepository userAccountRepository) {
        return username -> {
            UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
