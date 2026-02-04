package com.ecoride.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF disable karna zaroori hai Postman testing ke liye
                .cors(cors -> cors.disable()) // Agar frontend se problem ho toh ise bhi disable rakhein
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/register", "/users/login").permitAll() // Specifically open registration
                        .anyRequest().permitAll() // Testing ke liye sab allow kar rahe hain
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}