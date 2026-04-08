package com.ifreelife.carrertry.config;

import com.ifreelife.carrertry.common.RoleConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${RBAC_ENTERPRISE_PASSWORD:Enterprise@1234}")
    private String enterprisePassword;

    @Value("${RBAC_ADMIN_PASSWORD:Admin@1234}")
    private String adminPassword;

    @Value("${RBAC_STUDENT_PASSWORD:Student@1234}")
    private String studentPassword;

    @Value("${RBAC_SCHOOL_PASSWORD:School@1234}")
    private String schoolPassword;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .authorizeHttpRequests(auth -> auth
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
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
            User.withUsername("enterprise").password(passwordEncoder.encode(enterprisePassword)).roles(RoleConstants.ENTERPRISE).build(),
            User.withUsername("admin").password(passwordEncoder.encode(adminPassword)).roles(RoleConstants.ADMIN).build(),
            User.withUsername("student").password(passwordEncoder.encode(studentPassword)).roles(RoleConstants.STUDENT).build(),
            User.withUsername("school").password(passwordEncoder.encode(schoolPassword)).roles(RoleConstants.SCHOOL).build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
