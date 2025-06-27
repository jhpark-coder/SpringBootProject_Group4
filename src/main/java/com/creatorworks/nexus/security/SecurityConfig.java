package com.creatorworks.nexus.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * @Configuration: 이 클래스가 Spring의 설정 파일임을 나타냅니다.
 *                 애플리케이션이 시작될 때 Spring 컨테이너가 이 클래스를 읽어 설정을 구성합니다.
 * @EnableWebSecurity: Spring Security를 활성화하는 핵심 어노테이션입니다.
 *                     이 어노테이션이 있어야 CSRF, 로그인/로그아웃 등 보안 관련 기능들이 동작합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    // @Value("${file.upload-dir}")
    // private String uploadDir;
    
    // WebConfig의 리소스 핸들러 기능을 SecurityConfig로 통합
    // @Override
    // public void addResourceHandlers(ResourceHandlerRegistry registry) {
    //     String uploadPath = "file:" + System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
    //     registry.addResourceHandler("/uploads/**")
    //             .addResourceLocations(uploadPath);
    // }
    
    // 정적 리소스는 보안 필터 체인을 완전히 무시하도록 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
            "/static/**", "/js/**", "/css/**", "/assets/**", 
            "/uploads/**", "/h2-console/**", "/img/**");
    }

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
                // SPA 포워딩 경로를 CSRF 검증에서 제외
                .ignoringRequestMatchers("/editor", "/editor/**")
            )
            .authorizeHttpRequests(authz -> authz
                // '/editor/**' 경로는 SPA 컨트롤러가 처리하므로 permitAll()에 남겨둡니다.
                // 하지만 API 호출은 인증이 필요하므로 '/api/**'는 여기서 제외하고 아래에서 별도 처리합니다.
                .requestMatchers("/", "/editor/**", "/static/**", "/js/**", "/css/**", "/assets/**", "/h2-console/**", "/uploads/**", "/login", "/members/**", "/img/**").permitAll()
                // '/api/**' 경로는 인증된 사용자만 접근 가능하도록 설정
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            // 로그인 설정 추가
            .formLogin(formLogin -> formLogin
                .loginPage("/login")  // 로그인 페이지 경로
                .defaultSuccessUrl("/")  // 로그인 성공 시 리다이렉트
                .failureUrl("/login?error=true")  // 로그인 실패 시 리다이렉트
                .permitAll()
            )
            // 로그아웃 설정 추가
            .logout(logout -> logout
                .logoutSuccessUrl("/")  // 로그아웃 성공 시 리다이렉트
                .permitAll()
            )
            // HTTP 보안 헤더 추가
            .headers(headers -> headers
                .frameOptions().deny()  // 클릭재킹 방지
                .contentTypeOptions()  // MIME 스니핑 방지
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

    /**
     * 비밀번호 인코더 Bean
     * 회원가입 시 비밀번호를 안전하게 암호화하여 저장
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
