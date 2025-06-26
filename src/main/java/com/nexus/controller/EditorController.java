package com.nexus.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class EditorController {

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어있습니다.");
            }

            // 파일 확장자 확인
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 허용된 확장자 확인
            List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm", ".mp3", ".wav", ".ogg", ".m4a");
            if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
                return ResponseEntity.badRequest().body("지원하지 않는 파일 형식입니다.");
            }

            // 업로드 디렉토리 생성
            String uploadDir = "uploads";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 파일명 중복 방지
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            String filePath = uploadDir + File.separator + fileName;

            // 파일 저장
            File dest = new File(filePath);
            file.transferTo(dest);

            // URL 생성 (상대 경로)
            String fileUrl = "/uploads/" + fileName;

            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 중 오류가 발생했습니다.");
        }
    }
} 