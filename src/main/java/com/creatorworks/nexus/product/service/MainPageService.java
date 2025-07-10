package com.creatorworks.nexus.product.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.product.dto.ProductDto;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductHeart;
import com.creatorworks.nexus.product.repository.ProductHeartRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MainPageService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductHeartRepository productHeartRepository;
    private final OrderRepository orderRepository;
    private final RecentlyViewedProductRedisService recentlyViewedProductRedisService;

    private static final int RECOMMENDATION_LIMIT = 12; // 메인에 보여줄 상품 개수

    /**
     * 메인 페이지에 표시할 상품 목록을 조회합니다.
     * "선(先) 개인화 추천, 후(後) 인기 상품으로 채우기" 전략을 사용합니다.
     *
     * @param userEmail 현재 로그인한 사용자의 이메일 (비로그인 시 null)
     * @return 추천 상품 DTO 리스트
     */
    public List<ProductDto> getProductsForMainPage(String userEmail) {
        List<Product> finalProducts = new ArrayList<>();

        if (userEmail != null) {
            Member member = memberRepository.findByEmail(userEmail);
            if (member != null) {
                // --- 로그인 사용자 로직 ---
                log.info("로그인 사용자 '{}'의 추천 로직을 시작합니다.", userEmail);
                // 1. 개인화 추천 상품을 먼저 가져옵니다.
                List<Product> personalizedProducts = getPersonalizedRecommendations(member);
                finalProducts.addAll(personalizedProducts);

                // 2. 부족한 만큼 인기 상품으로 채웁니다 (이때 '제외 로직'이 필요).
                int needed = RECOMMENDATION_LIMIT - finalProducts.size();
                if (needed > 0) {
                    log.info("개인화 추천 후 부족한 {}개의 상품을 인기 상품으로 채웁니다.", needed);
                    List<Long> excludedIds = finalProducts.stream().map(Product::getId).collect(Collectors.toList());
                    finalProducts.addAll(getPopularProducts(needed, excludedIds));
                }
            }
        }

        // --- 비로그인 사용자 또는 member 정보가 없는 경우 ---
        if (finalProducts.isEmpty()) {
            log.info("비로그인 사용자 또는 개인화 추천 대상이 아니므로, 일반 인기 상품 추천을 시작합니다.");
            finalProducts.addAll(getPopularProducts(RECOMMENDATION_LIMIT));
        }

        log.info("최종적으로 {}개의 상품을 메인 페이지에 반환합니다.", finalProducts.size());
        return finalProducts.stream()
                .distinct() // 중복 제거
                .map(ProductDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 로그인한 사용자를 위한 개인화 추천 상품 목록을 생성합니다.
     * 사용자의 모든 활동(구매, 좋아요, 조회)을 기반으로 추천합니다.
     *
     * @param member 현재 사용자
     * @return 추천 상품 엔티티 리스트
     */
    private List<Product> getPersonalizedRecommendations(Member member) {
        // 1. 사용자 활동 기록 조회 (구매, 좋아요, 최근 조회)
        List<Order> purchasedOrders = orderRepository.findByBuyer(member);
        List<ProductHeart> heartedList = productHeartRepository.findByMember(member);
        List<Long> recentlyViewedIds = recentlyViewedProductRedisService.getRecentlyViewedProductIds(member.getId(), 50);
        List<Product> viewedProducts = productRepository.findAllById(recentlyViewedIds);

        // 2. [수정] 관심 카테고리를 1차, 2차로 분리하여 추출
        Set<String> primaryInterestCategories = new HashSet<>();
        Set<String> secondaryInterestCategories = new HashSet<>();

        Stream.concat(heartedList.stream().map(ProductHeart::getProduct), viewedProducts.stream())
                .forEach(product -> {
                    if (product.getPrimaryCategory() != null) {
                        primaryInterestCategories.add(product.getPrimaryCategory());
                    }
                    if (product.getSecondaryCategory() != null) {
                        secondaryInterestCategories.add(product.getSecondaryCategory());
                    }
                });

        if (primaryInterestCategories.isEmpty() && secondaryInterestCategories.isEmpty()) {
            log.info("사용자 {}의 분석 가능한 관심 카테고리가 없습니다.", member.getEmail());
            return Collections.emptyList();
        }
        // 더미 값 추가: 한 쪽이 비어있으면 쿼리에서 IN () 에러가 발생할 수 있으므로 방지.
        if(primaryInterestCategories.isEmpty()) primaryInterestCategories.add("__DUMMY__");
        if(secondaryInterestCategories.isEmpty()) secondaryInterestCategories.add("__DUMMY__");

        // 3. 추천에서 제외할 상품 ID 목록 (1.구매, 2.좋아요, 3.최근조회)
        List<Long> purchasedIds = purchasedOrders.stream()
                .filter(order -> order.getProduct() != null)
                .map(order -> order.getProduct().getId())
                .toList();
        List<Long> heartedIds = heartedList.stream().map(h -> h.getProduct().getId()).toList();

        // [수정] 수정 가능한 ArrayList로 생성하여 UnsupportedOperationException 방지
        List<Long> excludedIds = new ArrayList<>(Stream.of(purchasedIds.stream(), heartedIds.stream(), recentlyViewedIds.stream())
                .flatMap(s -> s)
                .distinct()
                .toList());

        if (excludedIds.isEmpty()) {
            excludedIds.add(-1L); // 쿼리 에러 방지
        }

        log.info("======= 개인화 추천 디버깅 (사용자: {}) =======", member.getEmail());
        log.info("관심 1차 카테고리: {}", primaryInterestCategories);
        log.info("관심 2차 카테고리: {}", secondaryInterestCategories);
        log.info("제외할 상품 ID 개수: {}", excludedIds.size());

        // 4. [수정] 새로운 Repository 메서드를 사용하여 추천 상품 조회
        Pageable limit = PageRequest.of(0, RECOMMENDATION_LIMIT);
        List<Product> recommendedProducts = productRepository.findByCategoriesAndExcludeIds(
                primaryInterestCategories,
                secondaryInterestCategories,
                excludedIds,
                limit
        );

        log.info("DB 조회 결과, 추천 상품 {}개를 찾았습니다.", recommendedProducts.size());
        log.info("======= 개인화 추천 디버깅 종료 =======");

        return recommendedProducts;
    }

    /**
     * 비로그인 사용자를 위한 인기 상품 목록 (제외 로직 없음)
     * @param limit 가져올 상품 개수
     * @return 인기 상품 리스트
     */
    private List<Product> getPopularProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        List<Long> topHeartedIds = productHeartRepository.findTopHeartedProductIds(pageable);
        if (!topHeartedIds.isEmpty()) {
            return productRepository.findByIdIn(topHeartedIds);
        }

        List<Long> topSellingIds = orderRepository.findTopSellingProductIds(pageable);
        if (!topSellingIds.isEmpty()) {
            return productRepository.findByIdIn(topSellingIds);
        }

        return productRepository.findAll(pageable).getContent();
    }

    /**
     * 로그인 사용자의 추천 목록을 채우기 위한 인기 상품 목록 (제외 로직 포함)
     * @param limit 가져올 상품 개수
     * @param excludedIds 제외할 상품 ID 목록
     * @return 인기 상품 리스트
     */
    private List<Product> getPopularProducts(int limit, List<Long> excludedIds) {
        Pageable pageable = PageRequest.of(0, limit);

        // [수정] 파라미터로 받은 리스트를 직접 수정하지 않고, 복사본을 만들어 사용
        List<Long> finalExcludedIds = new ArrayList<>(excludedIds);
        if (finalExcludedIds.isEmpty()) {
            finalExcludedIds.add(-1L); // 쿼리 오류 방지
        }

        List<Long> topHeartedIds = productHeartRepository.findTopHeartedProductIds(finalExcludedIds, pageable);
        if (!topHeartedIds.isEmpty()) {
            return productRepository.findByIdIn(topHeartedIds);
        }

        List<Long> topSellingIds = orderRepository.findTopSellingProductIds(finalExcludedIds, pageable);
        if (!topSellingIds.isEmpty()) {
            return productRepository.findByIdIn(topSellingIds);
        }

        return productRepository.findByIdNotIn(finalExcludedIds, pageable).getContent();
    }
} 