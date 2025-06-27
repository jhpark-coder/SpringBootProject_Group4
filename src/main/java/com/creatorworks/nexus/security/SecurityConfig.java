package com.creatorworks.nexus.security;

import com.creatorworks.nexus.member.service.MemberService;
import com.creatorworks.nexus.member.service.SocialMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2LoginSuccessHandler customOAuth2LoginSuccessHandler;
    private final SocialMemberService socialMemberService;
    private final MemberService memberService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings("removal")
    @Bean
    @Profile("dev")
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2Login -> oauth2Login
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(socialMemberService)
                )
                .successHandler(customOAuth2LoginSuccessHandler)
            )

            .formLogin(formLogin -> formLogin
                        .loginPage("/members/login")
                        .defaultSuccessUrl("/")
                        .usernameParameter("email")
                        .failureUrl("/members/login/error")
            )
            .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                        .logoutSuccessUrl("/")
            )
            .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                new AntPathRequestMatcher("/members/email-auth"),
                new AntPathRequestMatcher("/members/email-verify")
                )
            )
            .headers(headers -> headers.frameOptions().disable()); // H2 콘솔 사용을 위해

        return http.build();
    }
    
    @Bean
    @Profile("!dev")
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/editor/**", "/static/**", "/h2-console/**","/members/**").permitAll()
                .requestMatchers("/my-page").authenticated()
                // 위 규칙 외 나머지 모든 요청은 일단 허용 (개발 편의를 위해)
                .anyRequest().permitAll()
//                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2Login -> oauth2Login
            .defaultSuccessUrl("/")
            .userInfoEndpoint(userInfo -> userInfo
                .userService(socialMemberService)
            )
            .successHandler(customOAuth2LoginSuccessHandler)
        )
        .formLogin(formLogin -> formLogin
                    .loginPage("/members/login")
                    .defaultSuccessUrl("/")
                    .usernameParameter("email")
                    .failureUrl("/members/login/error")
        )
        .logout(logout -> logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                    .logoutSuccessUrl("/")
        )
        .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
        )
        .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                new AntPathRequestMatcher("/members/email-auth"),
                new AntPathRequestMatcher("/members/email-verify")
                )
        );
            
        
        return http.build();
    }
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }
}
