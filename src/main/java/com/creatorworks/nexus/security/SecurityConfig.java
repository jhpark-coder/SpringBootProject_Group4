package com.creatorworks.nexus.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
     * @Profile("dev"): 'dev' (개발) 프로필이 활성화되었을 때만 이 Bean 설정을 사용합니다.
     *                  src/main/resources/application.properties 파일의 'spring.profiles.active=dev' 설정과 연관됩니다.
     * @param http HttpSecurity 객체. Spring Security의 설정을 구성하기 위한 핵심 파라미터입니다.
     * @return 설정이 완료된 SecurityFilterChain 객체.
     * @throws Exception 설정 과정에서 발생할 수 있는 예외를 처리합니다.
     */
    @Bean
    @Profile("dev")
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        // http 객체를 사용하여 보안 설정을 연쇄적으로 구성합니다 (메소드 체이닝 방식).
        http
            // 1. 요청에 대한 인가(Authorization) 규칙 설정
            .authorizeHttpRequests(authz -> authz
                // .anyRequest(): 어떤 요청이 오든지
                // .permitAll(): 모두 허용한다. (개발 환경에서는 별도의 인증/인가 없이 모든 요청을 허용)
                .anyRequest().permitAll()
            )
            // 2. HTTP 헤더 관련 설정
            .headers(headers -> headers
                // .frameOptions().disable(): h2-console과 같이 <iframe>을 사용하는 페이지를 허용하기 위한 설정입니다.
                // 기본적으로 Spring Security는 Clickjacking 공격을 방지하기 위해 <iframe> 사용을 막습니다.
                .frameOptions().disable());
        
        // 3. 설정이 완료된 HttpSecurity 객체를 기반으로 SecurityFilterChain을 생성하여 반환합니다.
        return http.build();
    }
    
    /**
     * @Profile("!dev"): 'dev' (개발) 프로필이 아닐 때(!), 즉 'prod', 'staging' 등 다른 모든 프로필에서 이 설정을 사용합니다.
     *                   운영 환경을 위한 보안 설정입니다.
     */
    @Bean
    @Profile("!dev")
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. 요청에 대한 인가(Authorization) 규칙 설정
            .authorizeHttpRequests(authz -> authz
                // .requestMatchers(...): 지정된 URL 패턴에 해당하는 요청들을 의미합니다.
                // "/", "/editor/**", ... 등 지정된 경로는
                // .permitAll(): 인증/로그인 없이 모두 허용합니다.
                .requestMatchers("/", "/editor/**", "/api/**", "/static/**", "/js/**", "/h2-console/**").permitAll()
                // .anyRequest(): 위에서 지정한 경로 이외의 모든 요청은
                // .authenticated(): 반드시 인증(로그인)이 되어야만 접근 가능하도록 설정합니다.
                .anyRequest().authenticated()
            );
        
        // 'prod' 프로필에서는 .csrf().disable() 설정이 없으므로,
        // CSRF(Cross-Site Request Forgery) 보호 기능이 기본적으로 활성화됩니다.
        // 또한 frameOptions도 기본 설정(DENY)이 유지되어 <iframe> 사용이 차단됩니다.
        
        return http.build();
    }
}
