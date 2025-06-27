package com.creatorworks.nexus.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * JPA Auditing 기능을 위한 설정 클래스.
 * @EnableJpaAuditing 이 활성화되면, Spring은 반드시 AuditorAware 타입의 Bean을 찾는다.
 * 이 Bean이 없으면 Repository Bean들을 생성하는 단계에서 실패하여 애플리케이션이 시작되지 않는다.
 */
@Configuration
public class JpaConfig {

    /**
     * @CreatedBy, @LastModifiedBy 어노테이션이 붙은 필드에 값을 채워주기 위해
     * 현재 사용자가 누구인지를 알려주는 AuditorAware Bean을 등록한다.
     * @return 현재 감시자(사용자) 정보를 담은 Optional 객체.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // Spring Security의 보안 컨텍스트에서 Authentication 객체를 가져온다.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 인증 정보가 없거나, 인증되지 않았거나, 익명 사용자인 경우
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                // "system"을 기본값으로 반환한다.
                return Optional.of("system");
            }

            // 인증된 사용자가 있는 경우, 해당 사용자의 이름을 반환한다.
            // UserDetails를 구현한 Principal 객체를 사용할 경우, getName() 대신 getUsername()을 사용해야 할 수 있다.
            return Optional.of(authentication.getName());
        };
    }
} 