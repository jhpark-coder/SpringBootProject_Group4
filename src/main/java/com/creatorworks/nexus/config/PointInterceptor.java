package com.creatorworks.nexus.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 포인트 관련 인터셉터
 * 요청을 가로채서 포인트 관련 처리를 담당합니다.
 */
@Component
@Slf4j
public class PointInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 요청 URI 로깅
        String requestURI = request.getRequestURI();
        log.debug("PointInterceptor - Request URI: {}", requestURI);
        
        // 포인트 관련 요청인지 확인
        if (requestURI.contains("/points") || requestURI.contains("/purchase")) {
            log.debug("포인트 관련 요청 감지: {}", requestURI);
        }
        
        // 기본적으로 요청을 계속 진행
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 요청 완료 후 처리
        String requestURI = request.getRequestURI();
        int status = response.getStatus();
        
        log.debug("PointInterceptor - Request completed: {} - Status: {}", requestURI, status);
        
        if (ex != null) {
            log.error("PointInterceptor - Exception occurred: {}", ex.getMessage());
        }
    }
} 