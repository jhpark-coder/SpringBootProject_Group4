package com.creatorworks.nexus.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourcePath = "file:" + uploadDir + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourcePath);
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