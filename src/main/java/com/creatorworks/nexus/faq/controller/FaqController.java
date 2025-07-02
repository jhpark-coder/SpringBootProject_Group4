package com.creatorworks.nexus.faq.controller;

import com.creatorworks.nexus.faq.dto.FaqDto;
import com.creatorworks.nexus.faq.entity.FaqCategory;
import com.creatorworks.nexus.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqController {
    
    private final FaqService faqService;
    
    @GetMapping
    public ResponseEntity<List<FaqDto>> getAllFaqs() {
        return ResponseEntity.ok(faqService.getAllFaqs());
    }
    
    @GetMapping("/category")
    public ResponseEntity<List<FaqDto>> getFaqsByCategory(@RequestParam String name) {
        FaqCategory category = Arrays.stream(FaqCategory.values())
                .filter(c -> c.getDisplayName().equals(name))
                .findFirst()
                .orElse(null);

        if (category == null) {
            try {
                category = FaqCategory.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        return ResponseEntity.ok(faqService.getFaqsByCategory(category));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<FaqDto>> searchFaqs(@RequestParam String keyword) {
        return ResponseEntity.ok(faqService.searchFaqs(keyword));
    }
    
    @GetMapping("/popular")
    public ResponseEntity<List<FaqDto>> getPopularFaqs() {
        return ResponseEntity.ok(faqService.getPopularFaqs());
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categoryDisplayNames = Arrays.stream(FaqCategory.values())
                .map(FaqCategory::getDisplayName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDisplayNames);
    }
    
    @PostMapping("/{faqId}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long faqId) {
        faqService.incrementViewCount(faqId);
        return ResponseEntity.ok().build();
    }
} 