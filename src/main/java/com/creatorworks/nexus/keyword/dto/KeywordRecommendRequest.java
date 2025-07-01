package com.creatorworks.nexus.keyword.dto;

import java.util.List;

public class KeywordRecommendRequest {
    private List<String> keywords;

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "KeywordRecommendRequest{" +
                "keywords=" + keywords +
                '}';
    }
} 