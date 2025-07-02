package com.creatorworks.nexus.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.creatorworks.nexus.filter.RateLimitFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final RateLimitFilter rateLimitFilter;
    private final Bucket4jProperties bucket4jProperties;

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterBean() {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(rateLimitFilter);
        registrationBean.setOrder(1);
        
        // 모든 URL에 적용 (필터 내부에서 패턴 매칭)
        registrationBean.addUrlPatterns("/*");
        
        return registrationBean;
    }
} 