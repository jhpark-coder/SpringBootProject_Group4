package com.creatorworks.nexus.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Profile("dev")
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().disable()); // H2 콘솔 사용을 위해
        
        return http.build();
    }
    
    @Bean
    @Profile("!dev")
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/editor/**", "/static/**", "/js/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
