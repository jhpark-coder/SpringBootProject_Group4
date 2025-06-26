package com.creatorworks.nexus.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부에서 예상치 못한 오류가 발생했습니다.");
    }
} 