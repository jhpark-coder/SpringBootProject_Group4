package com.creatorworks.nexus.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 파일(이미지, 비디오 등)을 위한 리소스 핸들러 설정
        // /uploads/** 경로로 오는 요청을 실제 파일 시스템 경로에 매핑합니다.
        // 이렇게 하면 브라우저에서 /uploads/images/some-file.png 같은 URL로 파일에 접근할 수 있습니다.
        String uploadPath = "file:" + System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    @PostConstruct
    public void init() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        System.out.println("업로드 디렉토리: " + dir.getAbsolutePath());

        // 하위 폴더 생성
        String[] subfolders = {"images", "videos", "audios", "documents"};
        for (String folder : subfolders) {
            File subDir = new File(dir, folder);
            if (!subDir.exists()) {
                subDir.mkdirs();
            }
        }
        System.out.println("지원되는 하위 폴더: images/, videos/, audios/, documents/");
    }
} 