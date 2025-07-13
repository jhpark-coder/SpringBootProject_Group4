package com.creatorworks.nexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class PageableConfig {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return pageableResolver -> {
            pageableResolver.setMaxPageSize(100); // 최대 페이지 크기 설정
        };
    }

    @Bean("inquiryPageable")
    public PageRequest inquiryPageRequest() {
        return PageRequest.of(0, 4); // 문의 페이지 기본 설정
    }
} 