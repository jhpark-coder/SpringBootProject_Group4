package com.creatorworks.nexus.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PointInterceptor pointInterceptor;

    public WebMvcConfig(PointInterceptor pointInterceptor) {
        this.pointInterceptor = pointInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pointInterceptor)
                .addPathPatterns("/**")  // 모든 경로에 적용
                .excludePathPatterns("/api/**", "/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**"); // API와 정적 리소스 제외
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Editor SPA 라우팅
        registry.addResourceHandler("/editor/**")
                .addResourceLocations("classpath:/static/editor/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable() ? requestedResource
                                : new ClassPathResource("/static/editor/index.html");
                    }
                });
        
        // Chat Manager SPA 라우팅
        registry.addResourceHandler("/chat-manager", "/chat-manager/**")
                .addResourceLocations("classpath:/static/chat-manager/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable() ? requestedResource
                                : new ClassPathResource("/static/chat-manager/index.html");
                    }
                });
        
        // Chat Manager Assets 라우팅
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/chat-manager/assets/");

        //실시간 입찰 라우팅
        registry.addResourceHandler("/bidding/**")
                .addResourceLocations("classpath:/static/bidding/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable() ? requestedResource
                                : new ClassPathResource("/static/bidding/index.html");
                    }
                });
    }
} 