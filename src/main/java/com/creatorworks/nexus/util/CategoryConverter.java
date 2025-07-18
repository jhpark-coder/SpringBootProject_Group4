package com.creatorworks.nexus.util;

public class CategoryConverter {
    
    // 한글 1차 카테고리를 영어로 변환
    public static String convertPrimaryCategoryToEnglish(String koreanCategory) {
        if (koreanCategory == null) return null;
        
        switch (koreanCategory) {
            case "아트워크":
                return "artwork";
            case "그래픽디자인":
                return "graphic-design";
            case "캐릭터":
                return "character";
            case "프론트엔드":
                return "frontend";
            case "Python":
                return "python";
            case "Java":
                return "java";
            default:
                return koreanCategory; // 이미 영어인 경우 그대로 반환
        }
    }
    
    // 한글 2차 카테고리를 영어로 변환
    public static String convertSecondaryCategoryToEnglish(String koreanCategory) {
        if (koreanCategory == null) return null;
        
        switch (koreanCategory) {
            case "포토그래피":
                return "photography";
            case "일러스트레이션":
                return "illustration";
            case "스케치":
                return "sketch";
            case "코믹스":
                return "comics";
            case "타이포그래피":
                return "typography";
            case "앨범아트":
                return "album-art";
            case "로고":
                return "logo";
            case "브랜딩":
                return "branding";
            case "편집디자인":
                return "editorial-design";
            case "카툰":
                return "cartoon";
            case "팬아트":
                return "fan-art";
            case "2D 캐릭터":
                return "2d-character";
            case "3D 모델링":
                return "3d-modeling";
            case "HTML/CSS":
                return "html-css";
            case "JavaScript":
                return "javascript";
            case "React/Vue":
                return "react-vue";
            case "UI/UX":
                return "ui-ux";
            case "웹 개발":
                return "web-development";
            case "데이터 분석":
                return "data-analysis";
            case "머신러닝":
                return "machine-learning";
            case "자동화":
                return "automation";
            case "Spring/JPA":
                return "spring-jpa";
            case "네트워크":
                return "network";
            case "알고리즘":
                return "algorithm";
            case "코어 자바":
                return "core-java";
            default:
                return koreanCategory; // 이미 영어인 경우 그대로 반환
        }
    }
    
    // 영어 1차 카테고리를 한글로 변환
    public static String convertPrimaryCategoryToKorean(String englishCategory) {
        if (englishCategory == null) return null;
        
        switch (englishCategory.toLowerCase()) {
            case "artwork":
                return "아트워크";
            case "graphic-design":
                return "그래픽디자인";
            case "character":
                return "캐릭터";
            case "frontend":
                return "프론트엔드";
            case "python":
                return "Python";
            case "java":
                return "Java";
            default:
                return englishCategory; // 매핑되지 않은 경우 원본 반환
        }
    }
    
    // 영어 2차 카테고리를 한글로 변환
    public static String convertSecondaryCategoryToKorean(String englishCategory) {
        if (englishCategory == null) return null;
        
        switch (englishCategory.toLowerCase()) {
            case "photography":
                return "포토그래피";
            case "illustration":
                return "일러스트레이션";
            case "sketch":
                return "스케치";
            case "comics":
                return "코믹스";
            case "typography":
                return "타이포그래피";
            case "album-art":
                return "앨범아트";
            case "logo":
                return "로고";
            case "branding":
                return "브랜딩";
            case "editorial-design":
                return "편집디자인";
            case "cartoon":
                return "카툰";
            case "fan-art":
                return "팬아트";
            case "2d-character":
                return "2D 캐릭터";
            case "3d-modeling":
                return "3D 모델링";
            case "html-css":
                return "HTML/CSS";
            case "javascript":
                return "JavaScript";
            case "react-vue":
                return "React/Vue";
            case "ui-ux":
                return "UI/UX";
            case "web-development":
                return "웹 개발";
            case "data-analysis":
                return "데이터 분석";
            case "machine-learning":
                return "머신러닝";
            case "automation":
                return "자동화";
            case "spring-jpa":
                return "Spring/JPA";
            case "network":
                return "네트워크";
            case "algorithm":
                return "알고리즘";
            case "core-java":
                return "코어 자바";
            default:
                return englishCategory; // 매핑되지 않은 경우 원본 반환
        }
    }
} 