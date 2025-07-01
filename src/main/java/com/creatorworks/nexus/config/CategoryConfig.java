package com.creatorworks.nexus.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CategoryConfig {

    private final Map<String, List<String>> categories;

    public CategoryConfig() {
        Map<String, List<String>> categoryMap = new LinkedHashMap<>();
        categoryMap.put("artwork", List.of("포토그래피", "일러스트레이션", "스케치", "코믹스"));
        categoryMap.put("graphic-design", List.of("타이포그래피", "앨범아트", "로고", "브랜딩", "편집디자인"));
        categoryMap.put("character", List.of("카툰", "팬아트", "2D 캐릭터", "3D 모델링"));
        categoryMap.put("frontend", List.of("HTML/CSS", "JavaScript", "React/Vue", "UI/UX"));
        categoryMap.put("python", List.of("웹 개발", "데이터 분석", "머신러닝", "자동화"));
        categoryMap.put("java", List.of("Spring/JPA", "네트워크", "알고리즘", "코어 자바"));
        categories = Collections.unmodifiableMap(categoryMap);
    }

    public List<String> getSecondaryCategories(String primaryCategory) {
        return categories.getOrDefault(primaryCategory, Collections.emptyList());
    }

    public Map<String, List<String>> getAllCategories() {
        return categories;
    }
} 