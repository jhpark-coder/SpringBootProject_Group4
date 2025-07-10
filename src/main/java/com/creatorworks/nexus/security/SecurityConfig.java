package com.creatorworks.nexus.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.creatorworks.nexus.member.service.SocialMemberService;
import com.creatorworks.nexus.security.CustomLoginSuccessHandler;

import lombok.RequiredArgsConstructor;

/**
 * @Configuration: 이 클래스가 Spring의 설정 파일임을 나타냅니다.
 *                 애플리케이션이 시작될 때 Spring 컨테이너가 이 클래스를 읽어 설정을 구성합니다.
 * @EnableWebSecurity: Spring Security를 활성화하는 핵심 어노테이션입니다.
 *                     이 어노테이션이 있어야 CSRF, 로그인/로그아웃 등 보안 관련 기능들이 동작합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SocialMemberService socialMemberService;
    private final CustomOAuth2LoginSuccessHandler customOAuth2LoginSuccessHandler;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    // @Value("${file.upload-dir}")
    // private String uploadDir;
    
    //20250701 User < seller가 모든 권한 포함
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        // ADMIN은 SELLER의 권한을, SELLER는 USER의 권한을 포함한다는 규칙을 정의합니다.
        // 줄바꿈(\n)으로 여러 규칙을 정의할 수 있습니다.
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_SELLER\n" +
                "ROLE_SELLER > ROLE_USER");
        return roleHierarchy;
    }

    // 정적 리소스는 보안 필터 체인을 완전히 무시하도록 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
            "/favicon.ico", "/css/**", "/js/**", "/assets/**", 
            "/uploads/**", "/h2-console/**", "/img/**", "/images/**", "/.well-known/**", "/ws/**");
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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF 보호를 활성화하고, 토큰을 JS가 읽을 수 있는 쿠키로 생성합니다.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/", "/editor", "/h2-console/**", "/editor/api/upload", "/api/products/**", "/api/auctions/**", "/sentinel", "/api/korean/**", "/api/keyword/**", "/api/follow/**", "/api/faq/**", "/api/chat/**")
            )
            .authorizeHttpRequests(authz -> authz
                // 구체적인 경로를 먼저 설정
                .requestMatchers("/member/**").hasRole("USER")
                .requestMatchers("/editor/**", "/editor").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers("/api/auctions/**").authenticated()
                .requestMatchers("/api/follow/**").authenticated()
                .requestMatchers("/api/subscription/**").authenticated()
                .requestMatchers("/api/auction-payment/**").authenticated()
                .requestMatchers("/api/orders/points/charge-page").authenticated()
                // 그 외 모든 경로는 허용 (가장 넓은 범위를 나중에)
                .requestMatchers("/", "/sentinel", "/members/**", "/products/**", "/auction/**", "/members/logout", "/test/**", "/nestjstest", "/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/members/login")
                .usernameParameter("email")
                .successHandler(customLoginSuccessHandler)
                .defaultSuccessUrl("/")
                .failureUrl("/members/login/error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(socialMemberService))
                .successHandler(customOAuth2LoginSuccessHandler)
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()));
        return http.build();
    }
    
    @Bean
    @Profile("!dev")
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF 보호를 활성화하고, 토큰을 JS가 읽을 수 있는 쿠키로 생성합니다.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/", "/editor", "/h2-console/**", "/editor/api/upload", "/api/products/**", "/api/auctions/**", "/sentinel", "/api/korean/**", "/api/keyword/**", "/api/follow/**", "/api/faq/**", "/api/chat/**")
            )
            // 모든 요청을 허용합니다. (개발환경과 동일하게)
            .authorizeHttpRequests(authz -> authz
                // 구체적인 경로를 먼저 설정
                .requestMatchers("/member/**").hasRole("USER")
                .requestMatchers("/editor/**", "/editor").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers("/api/auctions/**").authenticated()
                .requestMatchers("/api/follow/**").authenticated()
                .requestMatchers("/api/subscription/**").authenticated()
                .requestMatchers("/api/auction-payment/**").authenticated()
                .requestMatchers("/api/orders/points/charge-page").authenticated()
                // 그 외 모든 경로는 허용
                .requestMatchers("/", "/sentinel", "/members/**", "/products/**", "/auction/**", "/members/logout", "/test/**", "/nestjstest", "/api/**").permitAll()
                .anyRequest().authenticated()
            )
            // 폼 로그인 설정
            .formLogin(formLogin -> formLogin
                .loginPage("/members/login")
                .usernameParameter("email")
                .successHandler(customLoginSuccessHandler)
                .defaultSuccessUrl("/")
                .failureUrl("/members/login/error")
                .permitAll()
            )
            // 로그아웃 설정
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(socialMemberService))
                .successHandler(customOAuth2LoginSuccessHandler)
            )
            // H2 콘솔을 위한 헤더 설정
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()));
        return http.build();
    }

    // CORS 설정을 위한 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 개발 서버 주소 및 알림 서버 주소 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
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