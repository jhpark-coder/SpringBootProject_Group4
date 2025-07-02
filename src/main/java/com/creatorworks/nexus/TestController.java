package com.creatorworks.nexus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final RedisTemplate<String, String> redisTemplate;
    
    // 접속 제한 설정
    private static final int RATE_LIMIT_PER_MINUTE = 10;
    private static final int RATE_LIMIT_PER_HOUR = 100;
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    /**
     * 접속 제한 테스트 페이지
     */
    @GetMapping("/test/rate-limit")
    public String rateLimitTestPage(Model model, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        model.addAttribute("clientIp", clientIp);
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return "test/rate-limit-test";
    }

    /**
     * API 호출 테스트 (접속 제한이 적용되는 엔드포인트)
     */
    @GetMapping("/api/test/rate-limit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testRateLimit(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        // 분당 제한 확인
        String minuteKey = RATE_LIMIT_KEY_PREFIX + clientIp + ":minute:" + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String hourKey = RATE_LIMIT_KEY_PREFIX + clientIp + ":hour:" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        
        try {
            // 분당 요청 수 확인
            String minuteCountStr = redisTemplate.opsForValue().get(minuteKey);
            int minuteCount = minuteCountStr != null ? Integer.parseInt(minuteCountStr) : 0;
            
            // 시간당 요청 수 확인
            String hourCountStr = redisTemplate.opsForValue().get(hourKey);
            int hourCount = hourCountStr != null ? Integer.parseInt(hourCountStr) : 0;
            
            // 제한 확인
            if (minuteCount >= RATE_LIMIT_PER_MINUTE) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "분당 요청 제한을 초과했습니다.");
                response.put("clientIp", clientIp);
                response.put("minuteCount", minuteCount);
                response.put("limitPerMinute", RATE_LIMIT_PER_MINUTE);
                response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                log.warn("분당 제한 초과 - IP: {}, 카운트: {}", clientIp, minuteCount);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
            }
            
            if (hourCount >= RATE_LIMIT_PER_HOUR) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "시간당 요청 제한을 초과했습니다.");
                response.put("clientIp", clientIp);
                response.put("hourCount", hourCount);
                response.put("limitPerHour", RATE_LIMIT_PER_HOUR);
                response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                log.warn("시간당 제한 초과 - IP: {}, 카운트: {}", clientIp, hourCount);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
            }
            
            // 카운터 증가
            minuteCount++;
            hourCount++;
            
            // Redis에 저장 (TTL 설정)
            redisTemplate.opsForValue().set(minuteKey, String.valueOf(minuteCount), 1, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(hourKey, String.valueOf(hourCount), 1, TimeUnit.HOURS);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "API 호출 성공!");
            response.put("clientIp", clientIp);
            response.put("minuteCount", minuteCount);
            response.put("hourCount", hourCount);
            response.put("limitPerMinute", RATE_LIMIT_PER_MINUTE);
            response.put("limitPerHour", RATE_LIMIT_PER_HOUR);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            log.info("API 호출 - IP: {}, 분당: {}/{}, 시간당: {}/{}", 
                    clientIp, minuteCount, RATE_LIMIT_PER_MINUTE, hourCount, RATE_LIMIT_PER_HOUR);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("접속 제한 처리 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "서버 오류가 발생했습니다.");
            response.put("clientIp", clientIp);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 접속 제한 상태 확인 API
     */
    @GetMapping("/api/test/rate-limit/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        String minuteKey = RATE_LIMIT_KEY_PREFIX + clientIp + ":minute:" + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String hourKey = RATE_LIMIT_KEY_PREFIX + clientIp + ":hour:" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        
        try {
            String minuteCountStr = redisTemplate.opsForValue().get(minuteKey);
            String hourCountStr = redisTemplate.opsForValue().get(hourKey);
            
            int minuteCount = minuteCountStr != null ? Integer.parseInt(minuteCountStr) : 0;
            int hourCount = hourCountStr != null ? Integer.parseInt(hourCountStr) : 0;
            
            Map<String, Object> response = new HashMap<>();
            response.put("clientIp", clientIp);
            response.put("minuteCount", minuteCount);
            response.put("hourCount", hourCount);
            response.put("limitPerMinute", RATE_LIMIT_PER_MINUTE);
            response.put("limitPerHour", RATE_LIMIT_PER_HOUR);
            response.put("minuteRemaining", Math.max(0, RATE_LIMIT_PER_MINUTE - minuteCount));
            response.put("hourRemaining", Math.max(0, RATE_LIMIT_PER_HOUR - hourCount));
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("접속 제한 상태 확인 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "상태 확인 중 오류가 발생했습니다.");
            response.put("clientIp", clientIp);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 접속 제한 카운터 리셋 (테스트용)
     */
    @PostMapping("/api/test/rate-limit/reset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetRateLimitCounter(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        try {
            // 현재 IP의 모든 접속 제한 키 삭제
            String minuteKey = RATE_LIMIT_KEY_PREFIX + clientIp + ":minute:" + 
                              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            String hourKey = RATE_LIMIT_KEY_PREFIX + clientIp + ":hour:" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
            
            redisTemplate.delete(minuteKey);
            redisTemplate.delete(hourKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "카운터가 리셋되었습니다.");
            response.put("clientIp", clientIp);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            log.info("접속 제한 카운터 리셋 - IP: {}", clientIp);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("카운터 리셋 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "카운터 리셋 중 오류가 발생했습니다.");
            response.put("clientIp", clientIp);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Redis 연결 상태 확인
     */
    @GetMapping("/api/test/redis-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkRedisStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Redis 연결 테스트
            redisTemplate.opsForValue().set("test:connection", "success");
            String result = redisTemplate.opsForValue().get("test:connection");
            redisTemplate.delete("test:connection");
            
            response.put("status", "success");
            response.put("message", "Redis 연결이 정상입니다.");
            response.put("testResult", result);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            log.info("Redis 연결 테스트 성공");
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Redis 연결에 실패했습니다.");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            log.error("Redis 연결 테스트 실패: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 