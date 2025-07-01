package com.creatorworks.nexus.keyword.controller;

import com.creatorworks.nexus.keyword.service.KoreanTextService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api/korean")
public class KoreanTextController {
    private final KoreanTextService koreanTextService;

    public KoreanTextController(KoreanTextService koreanTextService) {
        this.koreanTextService = koreanTextService;
        System.out.println("[DEBUG] KoreanTextController 빈 생성됨");
    }

    @PostMapping("/nouns")
    public List<String> extractNouns(@RequestBody Map<String, String> body) {
        System.out.println("[DEBUG] /api/korean/nouns 진입");
        System.out.println("[DEBUG] 요청 body: " + body);
        
        try {
            if (body == null) {
                System.out.println("[ERROR] body가 null입니다!");
                return Collections.emptyList();
            }
            
            String text = body.get("text");
            System.out.println("[DEBUG] 추출된 text: '" + text + "'");
            
            List<String> result = koreanTextService.extractNouns(text);
            System.out.println("[DEBUG] 서비스 응답: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("[ERROR] /api/korean/nouns 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
} 