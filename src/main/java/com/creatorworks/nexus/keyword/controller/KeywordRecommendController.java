package com.creatorworks.nexus.keyword.controller;

import com.creatorworks.nexus.keyword.dto.KeywordRecommendRequest;
import com.creatorworks.nexus.keyword.dto.KeywordRecommendResponse;
import com.creatorworks.nexus.keyword.service.KeywordRecommendService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keyword")
public class KeywordRecommendController {
    private final KeywordRecommendService keywordRecommendService;

    public KeywordRecommendController(KeywordRecommendService keywordRecommendService) {
        this.keywordRecommendService = keywordRecommendService;
        System.out.println("[DEBUG] KeywordRecommendController 빈 생성됨");
    }

    @PostMapping("/recommend")
    public KeywordRecommendResponse recommend(@RequestBody KeywordRecommendRequest request) {
        System.out.println("[DEBUG] /api/keyword/recommend 진입");
        System.out.println("[DEBUG] 요청값: " + request);
        try {
            KeywordRecommendResponse response = keywordRecommendService.recommendByKeywords(request);
            System.out.println("[DEBUG] 응답값: " + response);
            return response;
        } catch (Exception e) {
            System.out.println("[DEBUG] 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 