package com.creatorworks.nexus.config;

import java.time.temporal.ChronoUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "bucket4j")
public class Bucket4jProperties {
    
    private Redis redis = new Redis();
    private RateLimit rateLimit = new RateLimit();
    
    @Data
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
    }
    
    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private int capacity = 60;
        private int refillAmount = 60;
        private int refillPeriod = 1;
        private ChronoUnit refillUnit = ChronoUnit.MINUTES;
        private String includePaths;
        private String excludePaths;
        private String keyStrategy = "IP";
        private int expirationMinutes = 1;
    }
    
    public enum KeyStrategy {
        IP, USER_ID, CUSTOM
    }
} 