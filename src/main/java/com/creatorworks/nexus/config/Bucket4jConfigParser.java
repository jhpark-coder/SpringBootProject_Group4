package com.creatorworks.nexus.config;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class Bucket4jConfigParser {
    
    /**
     * 쉼표로 구분된 문자열을 List로 변환
     */
    public List<String> parseCommaSeparatedString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * 시간 단위 문자열을 ChronoUnit으로 변환
     */
    public ChronoUnit parseTimeUnit(String unit) {
        if (unit == null) {
            return ChronoUnit.MINUTES;
        }
        
        return switch (unit.toLowerCase()) {
            case "seconds" -> ChronoUnit.SECONDS;
            case "minutes" -> ChronoUnit.MINUTES;
            case "hours" -> ChronoUnit.HOURS;
            case "days" -> ChronoUnit.DAYS;
            default -> ChronoUnit.MINUTES;
        };
    }
    
    /**
     * 키 전략 문자열을 enum으로 변환
     */
    public Bucket4jProperties.KeyStrategy parseKeyStrategy(String strategy) {
        if (strategy == null) {
            return Bucket4jProperties.KeyStrategy.IP;
        }
        
        try {
            return Bucket4jProperties.KeyStrategy.valueOf(strategy.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Bucket4jProperties.KeyStrategy.IP;
        }
    }
} 