package com.ifreelife.carrertry.config;

import com.ifreelife.carrertry.common.RoleConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
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
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
            User.withUsername("enterprise").password("123456").roles(RoleConstants.ENTERPRISE).build(),
            User.withUsername("admin").password("123456").roles(RoleConstants.ADMIN).build(),
            User.withUsername("student").password("123456").roles(RoleConstants.STUDENT).build(),
            User.withUsername("school").password("123456").roles(RoleConstants.SCHOOL).build()
        );
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
