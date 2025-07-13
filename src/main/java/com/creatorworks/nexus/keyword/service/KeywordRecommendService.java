package com.creatorworks.nexus.keyword.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.creatorworks.nexus.keyword.dto.KeywordRecommendRequest;
import com.creatorworks.nexus.keyword.dto.KeywordRecommendResponse;
import com.creatorworks.nexus.keyword.dto.KeywordRecommendResponse.RecommendedProduct;
import com.creatorworks.nexus.keyword.specification.KeywordRecommendSpecification;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductItemTag;
import com.creatorworks.nexus.product.repository.ProductHeartRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;

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
        // 키워드 추천 처리 시작
        
        if (request == null) {
            // request가 null입니다!
            return new KeywordRecommendResponse();
        }
        
        List<String> keywords = request.getKeywords();
        // 추출된 키워드 정보
        
        if (keywords == null || keywords.isEmpty()) {
            // keywords가 null 또는 empty, 빈 응답 반환
            KeywordRecommendResponse emptyResponse = new KeywordRecommendResponse();
            emptyResponse.setProducts(new ArrayList<>());
            return emptyResponse;
        }
        
        // 키워드별 로그 출력
        
        try {
            // 1. 후보군 조회 (OR 조건)
            // Specification 생성 및 DB 조회
            Specification<Product> spec = KeywordRecommendSpecification.containsKeywordsInFields(keywords);
            List<Product> candidates = productRepository.findAll(spec);
            
            if (candidates.isEmpty()) {
                // 후보군이 없어서 빈 응답 반환
                KeywordRecommendResponse emptyResponse = new KeywordRecommendResponse();
                emptyResponse.setProducts(new ArrayList<>());
                return emptyResponse;
            }
            
            // 2. 각 Product별 점수 계산 및 태그 추출
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
                            // 태그 추출 중 오류
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
                        
                        // Product 점수 계산 완료
                        return dto;
                    } catch (Exception e) {
                        // Product 점수 계산 중 오류
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(dto -> dto.getScore() > 0)
                .collect(Collectors.toList());
                
            // 점수 1점 이상 product 개수
            
            // 3. 정렬 기준 변경: 키워드 점수가 최우선, 그 다음 최종 점수
            List<RecommendedProduct> top3 = scored.stream()
                .sorted(Comparator.comparingInt(RecommendedProduct::getScore).reversed() // 1. 키워드 점수
                    .thenComparing(Comparator.comparingDouble(RecommendedProduct::getFinalScore).reversed()) // 2. 최종 점수
                    .thenComparing(RecommendedProduct::getId, Comparator.reverseOrder())) // 3. ID
                .limit(3)
                .collect(Collectors.toList());
                
            // 최종 추천 top3 개수
            
            KeywordRecommendResponse response = new KeywordRecommendResponse();
            response.setProducts(top3);
            // recommendByKeywords 완료
            return response;
            
        } catch (Exception e) {
            // recommendByKeywords 실행 중 예외 발생
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