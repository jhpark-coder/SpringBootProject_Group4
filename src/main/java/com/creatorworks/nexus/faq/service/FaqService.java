package com.creatorworks.nexus.faq.service;

import com.creatorworks.nexus.faq.dto.FaqDto;
import com.creatorworks.nexus.faq.entity.Faq;
import com.creatorworks.nexus.faq.entity.FaqCategory;
import com.creatorworks.nexus.faq.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {
    
    private final FaqRepository faqRepository;
    
    public List<FaqDto> getAllFaqs() {
        return faqRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<FaqDto> getFaqsByCategory(FaqCategory category) {
        return faqRepository.findByCategoryAndIsActiveTrueOrderBySortOrderAsc(category)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<FaqDto> searchFaqs(String keyword) {
        return faqRepository.searchByKeyword(keyword)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<FaqDto> getPopularFaqs() {
        return faqRepository.findTop10ByViewCount()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void incrementViewCount(Long faqId) {
        faqRepository.findById(faqId).ifPresent(faq -> {
            faq.setViewCount(faq.getViewCount() + 1);
            faqRepository.save(faq);
        });
    }
    
    private FaqDto convertToDto(Faq faq) {
        FaqDto dto = new FaqDto();
        dto.setId(faq.getId());
        dto.setCategory(faq.getCategory());
        dto.setCategoryDisplayName(faq.getCategory().getDisplayName());
        dto.setQuestion(faq.getQuestion());
        dto.setAnswer(faq.getAnswer());
        dto.setSortOrder(faq.getSortOrder());
        dto.setIsActive(faq.getIsActive());
        dto.setViewCount(faq.getViewCount());
        dto.setCreatedAt(faq.getRegTime());
        dto.setUpdatedAt(faq.getUpdateTime());
        return dto;
    }
} 