package com.creatorworks.nexus.security;

import com.creatorworks.nexus.member.service.MemberService;
import com.creatorworks.nexus.member.service.SocialMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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

    private final MemberService memberService;
    private final SocialMemberService socialMemberService;
    private final CustomOAuth2LoginSuccessHandler customOAuth2LoginSuccessHandler;

    // @Value("${file.upload-dir}")
    // private String uploadDir;
    
    // 정적 리소스는 보안 필터 체인을 완전히 무시하도록 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
            "/favicon.ico", "/css/**", "/js/**", "/assets/**", 
            "/uploads/**", "/h2-console/**", "/img/**", "/.well-known/**");
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
            // CSRF 보호를 활성화하고, 토큰을 JS가 읽을 수 있는 쿠키로 생성합니다.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/", "/editor", "/h2-console/**", "/editor/api/upload", "/api/products/**", "/sentinel")
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/sentinel", "/members/**", "/products/**", "/auction/**").permitAll()
                .requestMatchers("/editor/**", "/editor").hasAnyRole("ADMIN", "SELLER")
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/members/login")
                .usernameParameter("email")
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
                .frameOptions().disable());
        return http.build();
    }
    
    @Bean
    @Profile("!dev")
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF 보호를 활성화하고, 토큰을 JS가 읽을 수 있는 쿠키로 생성합니다.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/", "/editor", "/h2-console/**", "/editor/api/upload", "/api/products/**", "/sentinel")
            )
            // 모든 요청을 허용합니다. (개발환경과 동일하게)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/sentinel", "/members/**", "/products/**", "/auction/**").permitAll()
                .requestMatchers("/editor/**", "/editor").hasAnyRole("ADMIN", "SELLER")
                .anyRequest().authenticated()
            )
            // 폼 로그인 설정
            .formLogin(formLogin -> formLogin
                .loginPage("/members/login")
                .usernameParameter("email")
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
                .frameOptions().disable());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(memberService);
        return new ProviderManager(provider);
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
