package com.creatorworks.nexus.keyword.dto;

import java.util.List;

public class KeywordRecommendResponse {
    private List<RecommendedProduct> products;

    public List<RecommendedProduct> getProducts() {
        return products;
    }

    public void setProducts(List<RecommendedProduct> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "KeywordRecommendResponse{" +
                "products=" + products +
                '}';
    }

    // 추천 결과로 반환할 작품 정보(간단 버전)
    public static class RecommendedProduct {
        private Long id;
        private String name;
        private String authorName;
        private String imageUrl;
        private String description;
        private String primaryCategory;
        private String secondaryCategory;
        private List<String> tags;
        private int score;
        
        // 정렬 기준용 추가 필드들
        private long viewCount;          // 조회수
        private long likeCount;          // 좋아요수  
        private long purchaseCount;      // 구매수 (향후 활성화 예정)
        private double finalScore;       // 가중치 적용 최종 점수

        // getter/setter
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAuthorName() { return authorName; }
        public void setAuthorName(String authorName) { this.authorName = authorName; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPrimaryCategory() { return primaryCategory; }
        public void setPrimaryCategory(String primaryCategory) { this.primaryCategory = primaryCategory; }
        public String getSecondaryCategory() { return secondaryCategory; }
        public void setSecondaryCategory(String secondaryCategory) { this.secondaryCategory = secondaryCategory; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        // 정렬 기준 필드 getter/setter
        public long getViewCount() { return viewCount; }
        public void setViewCount(long viewCount) { this.viewCount = viewCount; }
        public long getLikeCount() { return likeCount; }
        public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
        public long getPurchaseCount() { return purchaseCount; }
        public void setPurchaseCount(long purchaseCount) { this.purchaseCount = purchaseCount; }
        public double getFinalScore() { return finalScore; }
        public void setFinalScore(double finalScore) { this.finalScore = finalScore; }

        @Override
        public String toString() {
            return "RecommendedProduct{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", authorName='" + authorName + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", description='" + description + '\'' +
                    ", primaryCategory='" + primaryCategory + '\'' +
                    ", secondaryCategory='" + secondaryCategory + '\'' +
                    ", tags=" + tags +
                    ", score=" + score +
                    ", viewCount=" + viewCount +
                    ", likeCount=" + likeCount +
                    ", purchaseCount=" + purchaseCount +
                    ", finalScore=" + finalScore +
                    '}';
        }
    }
} 