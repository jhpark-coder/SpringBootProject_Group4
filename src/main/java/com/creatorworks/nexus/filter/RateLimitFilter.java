package com.creatorworks.nexus.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.creatorworks.nexus.config.Bucket4jConfigParser;
import com.creatorworks.nexus.config.Bucket4jProperties;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RedisTemplate<String, String> redisTemplate;
    private final Bucket4jProperties bucket4jProperties;
    private final Bucket4jConfigParser configParser;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String requestUri = httpRequest.getRequestURI();

        // Rate Limiting이 비활성화되어 있으면 통과
        if (!bucket4jProperties.getRateLimit().isEnabled()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // URL 패턴 확인
        if (!shouldApplyRateLimit(requestUri)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 키 생성
        String key = generateKey(httpRequest);
        
        // Rate Limit 확인
        if (isRateLimitExceeded(key)) {
            handleRateLimitExceeded(servletResponse, key);
            return;
        }

        // 요청 처리
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean shouldApplyRateLimit(String requestUri) {
        // 포함 패턴 확인
        String includePaths = bucket4jProperties.getRateLimit().getIncludePaths();
        if (includePaths != null && !includePaths.trim().isEmpty()) {
            boolean shouldInclude = configParser.parseCommaSeparatedString(includePaths)
                    .stream()
                    .anyMatch(pattern -> matchesPattern(requestUri, pattern));
            if (!shouldInclude) {
                return false;
            }
        }

        // 제외 패턴 확인
        String excludePaths = bucket4jProperties.getRateLimit().getExcludePaths();
        if (excludePaths != null && !excludePaths.trim().isEmpty()) {
            boolean shouldExclude = configParser.parseCommaSeparatedString(excludePaths)
                    .stream()
                    .anyMatch(pattern -> matchesPattern(requestUri, pattern));
            if (shouldExclude) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesPattern(String uri, String pattern) {
        // 간단한 패턴 매칭 (Ant 스타일 패턴을 단순화)
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return uri.startsWith(prefix);
        }
        return uri.equals(pattern);
    }

    private String generateKey(HttpServletRequest request) {
        Bucket4jProperties.KeyStrategy strategy = configParser.parseKeyStrategy(
                bucket4jProperties.getRateLimit().getKeyStrategy());
        
        return switch (strategy) {
            case IP -> RATE_LIMIT_KEY_PREFIX + getClientIp(request);
            case USER_ID -> {
                // TODO: 인증된 사용자 ID 사용
                yield RATE_LIMIT_KEY_PREFIX + getClientIp(request);
            }
            case CUSTOM -> {
                // TODO: 커스텀 키 생성 로직
                yield RATE_LIMIT_KEY_PREFIX + getClientIp(request);
            }
        };
    }

    private boolean isRateLimitExceeded(String key) {
        try {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            String minuteKey = key + ":minute:" + currentTime;
            
            // 현재 요청 수 확인
            String currentCountStr = redisTemplate.opsForValue().get(minuteKey);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
            
            // 제한 확인
            int capacity = bucket4jProperties.getRateLimit().getCapacity();
            if (currentCount >= capacity) {
                log.warn("Rate limit exceeded - Key: {}, Count: {}, Limit: {}", key, currentCount, capacity);
                return true;
            }
            
            // 카운터 증가
            redisTemplate.opsForValue().increment(minuteKey);
            redisTemplate.expire(minuteKey, 1, TimeUnit.MINUTES);
            
            return false;
            
        } catch (Exception e) {
            log.error("Rate limit check failed: {}", e.getMessage(), e);
            // 오류 발생 시 통과 (fail-open)
            return false;
        }
    }

    private void handleRateLimitExceeded(ServletResponse servletResponse, String key) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        httpResponse.setContentType("application/json");
        httpResponse.setStatus(429);
        
        String responseBody = String.format("""
                {
                    "error": "Too many requests",
                    "message": "Rate limit exceeded",
                    "key": "%s",
                    "timestamp": "%s"
                }
                """, key, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        httpResponse.getWriter().write(responseBody);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 