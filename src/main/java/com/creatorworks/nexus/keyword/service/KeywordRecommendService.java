package com.creatorworks.nexus.keyword.service;

import com.creatorworks.nexus.keyword.dto.KeywordRecommendRequest;
import com.creatorworks.nexus.keyword.dto.KeywordRecommendResponse;
import com.creatorworks.nexus.keyword.dto.KeywordRecommendResponse.RecommendedProduct;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductItemTag;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.product.repository.ProductHeartRepository;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.keyword.specification.KeywordRecommendSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeywordRecommendService {
    private final ProductRepository productRepository;
    private final ProductHeartRepository productHeartRepository;
    private final OrderRepository orderRepository;

    public KeywordRecommendService(ProductRepository productRepository, 
                                   ProductHeartRepository productHeartRepository,
                                   OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.productHeartRepository = productHeartRepository;
        this.orderRepository = orderRepository;
    }

    public KeywordRecommendResponse recommendByKeywords(KeywordRecommendRequest request) {
        System.out.println("[DEBUG] =================================");
        System.out.println("[DEBUG] recommendByKeywords 호출 시작");
        System.out.println("[DEBUG] 입력 request: " + request);
        
        if (request == null) {
            System.out.println("[ERROR] request가 null입니다!");
            return new KeywordRecommendResponse();
        }
        
        List<String> keywords = request.getKeywords();
        System.out.println("[DEBUG] 추출된 keywords: " + keywords);
        System.out.println("[DEBUG] keywords size: " + (keywords != null ? keywords.size() : "null"));
        
        if (keywords == null || keywords.isEmpty()) {
            System.out.println("[DEBUG] keywords가 null 또는 empty, 빈 응답 반환");
            KeywordRecommendResponse emptyResponse = new KeywordRecommendResponse();
            emptyResponse.setProducts(new ArrayList<>());
            return emptyResponse;
        }
        
        // 키워드별 로그 출력
        for (int i = 0; i < keywords.size(); i++) {
            System.out.println("[DEBUG] keyword[" + i + "]: '" + keywords.get(i) + "'");
        }
        
        try {
            // 1. 후보군 조회 (OR 조건)
            System.out.println("[DEBUG] Specification 생성 중...");
            Specification<Product> spec = KeywordRecommendSpecification.containsKeywordsInFields(keywords);
            System.out.println("[DEBUG] DB 조회 시작...");
            List<Product> candidates = productRepository.findAll(spec);
            System.out.println("[DEBUG] 후보군 product 개수: " + candidates.size());
            
            if (candidates.isEmpty()) {
                System.out.println("[DEBUG] 후보군이 없어서 빈 응답 반환");
                KeywordRecommendResponse emptyResponse = new KeywordRecommendResponse();
                emptyResponse.setProducts(new ArrayList<>());
                return emptyResponse;
            }
            
            // 2. 각 Product별 점수 계산 및 태그 추출
            System.out.println("[DEBUG] 점수 계산 시작...");
            List<RecommendedProduct> scored = candidates.stream()
                .map(product -> {
                    try {
                        RecommendedProduct dto = new RecommendedProduct();
                        dto.setId(product.getId());
                        dto.setName(product.getName());
                        dto.setSellerName(product.getSeller() != null ? product.getSeller().getName() : null);
                        dto.setImageUrl(product.getImageUrl());
                        dto.setDescription(product.getDescription());
                        dto.setPrimaryCategory(product.getPrimaryCategory());
                        dto.setSecondaryCategory(product.getSecondaryCategory());
                        
                        // 태그명 리스트 추출 (더 안전한 방식)
                        List<String> tagNames = new ArrayList<>();
                        try {
                            if (product.getItemTags() != null && !product.getItemTags().isEmpty()) {
                                tagNames = product.getItemTags().stream()
                                    .filter(Objects::nonNull)
                                    .map(ProductItemTag::getItemTag)
                                    .filter(Objects::nonNull)
                                    .map(itemTag -> itemTag.getName())
                                    .filter(Objects::nonNull)
                                    .filter(name -> !name.trim().isEmpty())
                                    .collect(Collectors.toList());
                            }
                        } catch (Exception e) {
                            System.out.println("[ERROR] 태그 추출 중 오류 (Product ID: " + product.getId() + "): " + e.getMessage());
                            tagNames = Collections.emptyList();
                        }
                        dto.setTags(tagNames);
                        
                        // 정렬 기준 데이터 설정
                        dto.setViewCount(product.getViewCount());
                        dto.setLikeCount(productHeartRepository.countByProductId(product.getId()));
                        dto.setPurchaseCount(orderRepository.countByProductId(product.getId())); // 테스트 데이터로 활성화!
                        
                        // 점수 계산
                        int score = 0;
                        for (String keyword : keywords) {
                            if (keyword == null || keyword.trim().isEmpty()) continue;
                            
                            String lower = keyword.toLowerCase().trim();
                            
                            if (containsIgnoreCase(product.getName(), lower)) score += 100;
                            if (containsIgnoreCase(product.getDescription(), lower)) score += 100;
                            if (containsIgnoreCase(product.getWorkDescription(), lower)) score += 100;
                            if (containsIgnoreCase(product.getTiptapJson(), lower)) score += 100;
                            if (tagNames.stream().anyMatch(tag -> containsIgnoreCase(tag, lower))) score += 100;
                            if (product.getSeller() != null && containsIgnoreCase(product.getSeller().getName(), lower)) score += 100;
                            if (containsIgnoreCase(product.getSecondaryCategory(), lower)) score += 50;
                            if (containsIgnoreCase(product.getPrimaryCategory(), lower)) score += 30;
                        }
                        dto.setScore(score);
                        
                        double finalScore = score +                          // 키워드 매칭 점수 (기본)
                                          (dto.getPurchaseCount() * 0.6) +  // 구매수: 0.6점/개 (가장 높은 가치)
                                          (dto.getLikeCount() * 0.3) +      // 좋아요: 0.3점/개 (중간 가치)
                                          (dto.getViewCount() * 0.05);       // 조회수: 0.05점/회 (참고용)
                        dto.setFinalScore(finalScore);
                        
                        System.out.println("[DEBUG] Product ID " + product.getId() + " (" + product.getName() + 
                                         ") 키워드점수: " + score + ", 구매: " + dto.getPurchaseCount() + 
                                         ", 좋아요: " + dto.getLikeCount() + ", 조회수: " + dto.getViewCount() + 
                                         " → 최종점수: " + String.format("%.1f", finalScore));
                        return dto;
                    } catch (Exception e) {
                        System.out.println("[ERROR] Product 점수 계산 중 오류 (ID: " + product.getId() + "): " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(dto -> dto.getScore() > 0)
                .collect(Collectors.toList());
                
            System.out.println("[DEBUG] 점수 1점 이상 product 개수: " + scored.size());
            
            // 3. 정렬 기준 변경: 키워드 점수가 최우선, 그 다음 최종 점수
            List<RecommendedProduct> top3 = scored.stream()
                .sorted(Comparator.comparingInt(RecommendedProduct::getScore).reversed() // 1. 키워드 점수
                    .thenComparing(Comparator.comparingDouble(RecommendedProduct::getFinalScore).reversed()) // 2. 최종 점수
                    .thenComparing(RecommendedProduct::getId, Comparator.reverseOrder())) // 3. ID
                .limit(3)
                .collect(Collectors.toList());
                
            System.out.println("[DEBUG] 최종 추천 top3 개수: " + top3.size());
            for (int i = 0; i < top3.size(); i++) {
                RecommendedProduct p = top3.get(i);
                System.out.println("[DEBUG] 🏆 Top" + (i+1) + ": " + p.getName() + 
                                 " (최종점수: " + String.format("%.1f", p.getFinalScore()) + 
                                 " = 키워드:" + p.getScore() + " + 구매:" + p.getPurchaseCount() + 
                                 " + 좋아요:" + p.getLikeCount() + " + 조회수:" + p.getViewCount() + ")");
            }
            
            KeywordRecommendResponse response = new KeywordRecommendResponse();
            response.setProducts(top3);
            System.out.println("[DEBUG] 최종 응답: " + response);
            System.out.println("[DEBUG] recommendByKeywords 완료");
            System.out.println("[DEBUG] =================================");
            return response;
            
        } catch (Exception e) {
            System.out.println("[ERROR] recommendByKeywords 실행 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            KeywordRecommendResponse errorResponse = new KeywordRecommendResponse();
            errorResponse.setProducts(new ArrayList<>());
            return errorResponse;
        }
    }

    private boolean containsIgnoreCase(String field, String keyword) {
        if (field == null || keyword == null) return false;
        if (field.trim().isEmpty() || keyword.trim().isEmpty()) return false;
        return field.toLowerCase().contains(keyword.toLowerCase());
    }
} 