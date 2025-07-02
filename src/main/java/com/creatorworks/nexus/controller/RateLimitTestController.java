package com.creatorworks.nexus.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatorworks.nexus.config.Bucket4jProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test/rate-limit")
@RequiredArgsConstructor
@Slf4j
public class RateLimitTestController {

    private final Bucket4jProperties bucket4jProperties;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", bucket4jProperties.getRateLimit().isEnabled());
        response.put("capacity", bucket4jProperties.getRateLimit().getCapacity());
        response.put("refillAmount", bucket4jProperties.getRateLimit().getRefillAmount());
        response.put("refillPeriod", bucket4jProperties.getRateLimit().getRefillPeriod());
        response.put("refillUnit", bucket4jProperties.getRateLimit().getRefillUnit());
        response.put("includePaths", bucket4jProperties.getRateLimit().getIncludePaths());
        response.put("excludePaths", bucket4jProperties.getRateLimit().getExcludePaths());
        response.put("keyStrategy", bucket4jProperties.getRateLimit().getKeyStrategy());
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testRateLimit() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rate limit test successful");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("limit", bucket4jProperties.getRateLimit().getCapacity());
        
        log.info("Rate limit test endpoint called");
        
        return ResponseEntity.ok(response);
    }
} 