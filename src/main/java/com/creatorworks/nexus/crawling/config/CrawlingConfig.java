package com.creatorworks.nexus.crawling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "crawling")
public class CrawlingConfig {
    
    private Loud loud = new Loud();
    private Selectors selectors = new Selectors();
    private Timeout timeout = new Timeout();
    private int maxRetries = 3;
    private int delayBetweenRequestsMs = 1000;
    
    @Data
    public static class Loud {
        private String username;
        private String password;
        private String baseUrl;
        private String loginUrl;
        private String contestUrl;
    }
    
    @Data
    public static class Selectors {
        // 로그인 관련
        private String usernameField;
        private String passwordField;
        private String loginButton;
        
        // 콘테스트 목록 페이지
        private String contestItem;
        
        // 우승자 정보 컨테이너
        private String winnerContainer;
        
        // 작가명
        private String authorName;
        
        // 썸네일 이미지
        private String thumbnailImg;
        
        // 콘테스트 제목
        private String contestTitle;
        
        // 태그
        private String tagItem;
        
        // 상세 이미지들
        private String detailImages;
    }
    
    @Data
    public static class Timeout {
        private int seconds = 10;
    }
} 