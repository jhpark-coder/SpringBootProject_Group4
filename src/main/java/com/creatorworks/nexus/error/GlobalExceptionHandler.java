package com.creatorworks.nexus.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleMultipartException(MultipartException e) {
        log.error("파일 업로드 중 오류 발생: {}", e.getMessage());

        if (e instanceof MaxUploadSizeExceededException) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("파일 크기가 너무 큽니다. 설정된 최대 크기를 초과했습니다.");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("파일 업로드 요청 처리 중 오류가 발생했습니다. 요청 형식을 확인해주세요.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        String requestUri = request.getDescription(false);
        
        // Chrome DevTools 관련 요청은 로그에서 제외
        if (requestUri.contains(".well-known/appspecific/com.chrome.devtools.json") || 
            requestUri.contains("favicon.ico") ||
            requestUri.contains("robots.txt")) {
            // 로그를 출력하지 않고 조용히 처리
            return "error/404";
        }
        
        log.error("404 Not Found: 요청한 리소스를 찾을 수 없습니다. URL: {}", requestUri, ex);
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, WebRequest request) {
        log.error("예상치 못한 오류 발생. URL: {}", request.getDescription(false), ex);
        return "error/500";
    }
} 