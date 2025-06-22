package com.creatorworks.nexus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 요청을 실제 파일 시스템 경로로 매핑 (개발환경 대응)
        // 타입별 하위 폴더들도 모두 포함됨 (images/, videos/, audios/, documents/)
        String uploadPath = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
                
        System.out.println("Static resource mapping: /uploads/** -> " + uploadPath);
        System.out.println("지원되는 하위 폴더: images/, videos/, audios/, documents/");
    }
} 