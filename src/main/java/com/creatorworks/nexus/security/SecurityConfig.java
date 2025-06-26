package com.creatorworks.nexus.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

/**
 * @Configuration: 이 클래스가 Spring의 설정 파일임을 나타냅니다.
 *                 애플리케이션이 시작될 때 Spring 컨테이너가 이 클래스를 읽어 설정을 구성합니다.
 * @EnableWebSecurity: Spring Security를 활성화하는 핵심 어노테이션입니다.
 *                     이 어노테이션이 있어야 CSRF, 로그인/로그아웃 등 보안 관련 기능들이 동작합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * @Bean: 이 메소드가 반환하는 객체(SecurityFilterChain)를 Spring 컨테이너가 관리하는 Bean으로 등록합니다.
     *        Spring이 필요할 때마다 이 메소드를 호출하여 보안 필터 체인을 가져와 사용합니다.
     * @param http HttpSecurity 객체. Spring Security의 설정을 구성하기 위한 핵심 파라미터입니다.
     * @return 설정이 완료된 SecurityFilterChain 객체.
     * @throws Exception 설정 과정에서 발생할 수 있는 예외를 처리합니다.
     */
    @Bean
    @Profile("dev")
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            // 개발 환경에서는 CORS 설정도 동일하게 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 개발 환경에서는 CSRF 보호 기능을 명시적으로 비활성화
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            .headers(headers -> headers
                .frameOptions().disable());
        return http.build();
    }
    
    @Bean
    @Profile("!dev")
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/editor/**", "/api/**", "/static/**", "/js/**", "/css/**", "/assets/**", "/h2-console/**", "/uploads/**").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }

    // CORS 설정을 위한 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 개발 서버 주소 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
