package com.creatorworks.nexus.member.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.creatorworks.nexus.member.dto.CustomUserDetails;
import com.creatorworks.nexus.member.dto.FollowingDto;
import com.creatorworks.nexus.member.dto.MonthlyCategoryPurchaseDTO;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberOrderRepository;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.service.MemberFollowService;
import com.creatorworks.nexus.notification.entity.Notification;
import com.creatorworks.nexus.notification.service.NotificationService;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.service.PointService;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.product.service.ProductService;
import com.creatorworks.nexus.product.service.RecentlyViewedProductRedisService;
import com.creatorworks.nexus.security.dto.UserAccount;

import lombok.RequiredArgsConstructor;


//20250630 차트 생성을 위해 작성됨
@Controller
@RequiredArgsConstructor
public class MyPageController {

    private static final Logger log = LoggerFactory.getLogger(MyPageController.class);

    private final RecentlyViewedProductRedisService recentlyViewedProductRedisService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ProductRepository productRepository;
    private final MemberOrderRepository memberOrderRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final PointService pointService;
    private final ProductService productService;
    private final MemberFollowService memberFollowService;

    @GetMapping({"/User/my-page", "/members/my-page"})
    public String myPage(@AuthenticationPrincipal Object principal, Model model) {
        try {
            // 1. 현재 로그인한 사용자의 ID를 안전하게 가져오기
            log.info("마이페이지 접근 - Principal 타입: {}", principal != null ? principal.getClass().getSimpleName() : "null");
            
            String email = getEmailFromPrincipal(principal);
            log.info("마이페이지 접근 - 이메일: {}", email);
            
            Member currentMember = memberRepository.findByEmail(email);
            log.info("마이페이지 접근 - 회원 조회 결과: {}", currentMember != null ? "성공" : "실패");
            
            if (currentMember == null) {
                log.error("회원 정보를 찾을 수 없습니다: {}", email);
                throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
            }

        Long currentMemberId = currentMember.getId();

        // 2. 차트 데이터 조회 기간 설정 (최근 6개월)
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(6).withDayOfMonth(1).toLocalDate().atStartOfDay();

        // --- 디버깅 로그 추가 ---
        log.info("차트 데이터 조회 시작. 사용자 ID: {}, 기간: {} ~ {}", currentMemberId, startDate, endDate);
        // -------------------------

        // 3. DB에서 월별/카테고리별 구매 데이터 조회
        List<MonthlyCategoryPurchaseDTO> purchases = memberOrderRepository.findMonthlyCategoryPurchases(currentMemberId, startDate, endDate);

        // --- ★★★★★ 여기가 가장 중요! 조회된 결과 크기 확인 ★★★★★ ---
        log.info("DB 조회 완료. 조회된 구매 기록 건수: {}", purchases.size());
        // purchases 리스트의 실제 내용을 보고 싶다면:
        // purchases.forEach(p -> log.info("  - {}", p));
        // -----------------------------------------------------------

        // 4. Chart.js 데이터 구조로 가공
        List<String> labels = Stream.iterate(startDate.toLocalDate(), date -> date.plusMonths(1))
                .limit(6)
                .map(date -> date.getMonthValue() + "월")
                .collect(Collectors.toList());
        // =================================================================
        // 4-1. 모든 primaryCategory의 목록을 중복 없이 가져와 정렬합니다.
        List<String> categories = purchases.stream()
                .map(MonthlyCategoryPurchaseDTO::primaryCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

// 4-2. 카테고리별로 랜덤 색상을 지정합니다. (더 예쁜 색상 팔레트를 사용해도 좋습니다)
        Map<String, String> categoryColorMap = new HashMap<>();
        List<String> colors = List.of("#4E79A7", "#F28E2B", "#E15759", "#76B7B2", "#59A14F", "#EDC948");
        for (int i = 0; i < categories.size(); i++) {
            categoryColorMap.put(categories.get(i), colors.get(i % colors.size()));
        }

// 4-3. 최종 datasets를 만듭니다.
        List<Map<String, Object>> datasets = new ArrayList<>();
        for (String categoryName : categories) {
            Map<String, Object> dataset = new LinkedHashMap<>();
            dataset.put("label", categoryName); // 범례 이름

            // 6개월치 데이터를 담을 리스트 (초기값은 모두 0)
            List<Long> data = new ArrayList<>(Collections.nCopies(6, 0L));

            // 해당 카테고리의 구매 기록만 필터링
            purchases.stream()
                    .filter(p -> p.primaryCategory().equals(categoryName))
                    .forEach(p -> {
                        // 월(month) 정보를 인덱스로 변환 (예: 1월->0, 2월->1 ...)
                        int monthIndex = p.month() - 1; // 1월은 0번째 인덱스
                        if (monthIndex >= 0 && monthIndex < 6) {
                            data.set(monthIndex, p.count()); // 해당 월의 데이터 업데이트
                        }
                    });

            dataset.put("data", data);
            dataset.put("backgroundColor", categoryColorMap.get(categoryName)); // 카테고리별 색상
            datasets.add(dataset);
        }

// ----------------------------------------------------

        // 5. 최종 데이터를 Model에 담아 View로 전달
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartDatasets", datasets);

        // --- 디버깅 로그 추가 ---
        log.info("프론트로 전달될 Datasets 건수: {}", datasets.size());
        // -------------------------

        // ==========================================================
        //      ★★★ 최근 구매 상품 목록 조회 로직 추가 ★★★
        // ==========================================================

        // 1. 페이징 정보 생성: 0번째 페이지에서 4개의 항목을 가져온다.
        Pageable topFour = PageRequest.of(0, 4);

        // 2. DB에서 최근 구매한 주문 4개를 가져온다.
        org.springframework.data.domain.Page<Order> recentOrdersPage = orderRepository.findByBuyerOrderByOrderDateDesc(currentMember, topFour);

        // 3. Order 목록에서 Product 목록만 추출한다.
        //    (ProductDto를 사용하면 더 좋습니다. 여기서는 간단히 Product 엔티티를 그대로 사용합니다)
        List<Product> recentProducts = recentOrdersPage.getContent().stream()
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getProduct() != null)
                .map(item -> item.getProduct())
                .distinct()
                .collect(Collectors.toList());

        // 4. Model에 'recentProducts'라는 이름으로 담아서 View로 전달한다.
        model.addAttribute("recentProducts", recentProducts);

        // ==========================================================
        //      ★★★ Redis 최근 본 상품 - 서비스 사용 로직으로 수정 ★★★
        // ==========================================================

        // 1. 서비스 호출하여 최근 본 상품 ID 목록을 10개 가져옴
        List<Long> recentProductIds = recentlyViewedProductRedisService.getRecentlyViewedProductIds(currentMemberId, 10);

        if (!recentProductIds.isEmpty()) {
            // 2. ID 목록으로 DB에서 상품 정보들을 한 번에 조회(IN 쿼리)
            List<Product> unorderedProducts = productRepository.findAllById(recentProductIds);

            // 3. DB에서 가져온 상품들을 Redis에서 가져온 ID 순서(최신순)로 재정렬
            Map<Long, Product> productMap = unorderedProducts.stream()
                    .collect(Collectors.toMap(Product::getId, product -> product));

            List<Product> sortedProducts = recentProductIds.stream()
                    .map(productMap::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            // 4. 구매하지 않은 상품만 필터링하고 4개로 제한
            List<Product> recentViewedProducts = sortedProducts.stream()
                    .filter(product -> currentMember != null && !orderRepository.existsByBuyerAndProduct(currentMember, product))
                    .limit(4)
                    .collect(Collectors.toList());

            model.addAttribute("recentViewedProducts", recentViewedProducts);
        } else {
            model.addAttribute("recentViewedProducts", Collections.emptyList());
        }

        // ==========================================================


        // ==========================================================
        //      ★★★ 카테고리 통계 조회/가공 로직 추가 ★★★
        // ==========================================================
        Map<String, Long> totalCounts = new HashMap<>();

        // 1. 최근 7일간의 모든 카테고리 조회수를 합산
        for (int i = 0; i < 7; i++) {
            String dailyCountKey = "categoryViewCount:" + LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            Set<ZSetOperations.TypedTuple<String>> dailyTuples = redisTemplate.opsForZSet().rangeWithScores(dailyCountKey, 0, -1);

            if (dailyTuples != null) {
                for (ZSetOperations.TypedTuple<String> tuple : dailyTuples) {
                    String categoryMember = tuple.getValue();
                    long count = tuple.getScore().longValue();
                    totalCounts.merge(categoryMember, count, Long::sum);
                }
            }
        }

        // 2. Primary Category별로 데이터 가공
        Map<String, Long> primaryCategoryCounts = totalCounts.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().split(":")[0], // "artwork:포토그래피" -> "artwork"
                        Collectors.summingLong(Map.Entry::getValue)
                ));

        // 3. Secondary Category별로 데이터 가공 (primary를 key로 가짐)
        Map<String, Map<String, Long>> secondaryCategoryData = totalCounts.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().split(":")[0], // "artwork"
                        Collectors.toMap(
                                entry -> entry.getKey().split(":")[1], // "포토그래피"
                                Map.Entry::getValue
                        )
                ));

        model.addAttribute("primaryCategoryData", primaryCategoryCounts);
        model.addAttribute("secondaryCategoryData", secondaryCategoryData);
        
        // 사용자 이름 추가
        model.addAttribute("Name", currentMember.getName());
        
        // ==========================================================

        return "member/myPage"; // myPage.html 템플릿을 보여줌
        } catch (Exception e) {
            log.error("마이페이지 접근 중 오류 발생", e);
            throw e;
        }
    }

    @GetMapping({"/User/my-page/notifications", "/members/my-page/notifications"})
    public String notificationHistory(@AuthenticationPrincipal Object principal, Model model) {
        String email = getEmailFromPrincipal(principal);
        Member currentUser = memberRepository.findByEmail(email);
        if (currentUser != null) {
            // 관리자 권한 확인
            boolean isAdmin = false;
            if (principal instanceof CustomUserDetails) {
                isAdmin = ((CustomUserDetails) principal).getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            }
            
            List<Notification> notifications;
            if (isAdmin) {
                // 관리자의 경우 개인 알림 + 관리자 알림 조회
                notifications = notificationService.getNotificationsForAdmin(currentUser.getId());
            } else {
                // 일반 사용자의 경우 개인 알림만 조회
                notifications = notificationService.getNotificationsByUserId(currentUser.getId());
            }
            model.addAttribute("notifications", notifications);
        } else {
            model.addAttribute("notifications", Collections.emptyList());
        }
        
        return "member/my-notifications"; // 알림 내역 페이지 템플릿 반환
    }

    @GetMapping("/members/liked-products")
    public String likedProducts(@AuthenticationPrincipal Object principal, Model model) {
        try {
            String email = getEmailFromPrincipal(principal);
            Member currentMember = memberRepository.findByEmail(email);
            
            if (currentMember == null) {
                log.error("회원 정보를 찾을 수 없습니다: {}", email);
                throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
            }
            
            // 1. 현재 사용자가 좋아요한 상품 목록을 조회
            List<Product> likedProducts = productService.findLikedProductsByUser(email);

            model.addAttribute("likedProducts", likedProducts);
            model.addAttribute("Name", currentMember.getName());
            
            return "member/likedProducts";
        } catch (Exception e) {
            log.error("좋아요한 상품 페이지 로드 중 오류 발생", e);
            return "redirect:/";
        }
    }
    


    @GetMapping("/members/followings")
    public String followings(@AuthenticationPrincipal Object principal, 
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        try {
            String email = getEmailFromPrincipal(principal);
            Member currentMember = memberRepository.findByEmail(email);
            
            if (currentMember == null) {
                log.error("회원 정보를 찾을 수 없습니다: {}", email);
                throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
            }
            
            // 팔로잉 목록 조회 (페이징 적용)
            List<FollowingDto> allFollowings = memberFollowService.getFollowingsWithDate(currentMember.getId());
            long followingCount = allFollowings.size();
            
            // 페이징 처리
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, allFollowings.size());
            List<FollowingDto> followings = allFollowings.subList(startIndex, endIndex);
            
            int totalPages = (int) Math.ceil((double) followingCount / size);
            boolean hasPrevious = page > 0;
            boolean hasNext = page < totalPages - 1;
            
            model.addAttribute("followings", followings);
            model.addAttribute("followingCount", followingCount);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("hasPrevious", hasPrevious);
            model.addAttribute("hasNext", hasNext);
            
            return "member/followings";
        } catch (Exception e) {
            log.error("팔로잉 목록 페이지 로드 중 오류 발생", e);
            return "redirect:/";
        }
    }

    @GetMapping({"/User/my-page/points", "/members/my-page/points"})
    public String myPointHistory(@AuthenticationPrincipal Object principal, 
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 Model model) {
        try {
            String email = getEmailFromPrincipal(principal);
            Member currentMember = memberRepository.findByEmail(email);

            if (currentMember == null) {
                log.error("회원 정보를 찾을 수 없습니다: {}", email);
                model.addAttribute("currentBalance", 0L);
                model.addAttribute("pointHistoryPage", null);
                model.addAttribute("page", page);
                model.addAttribute("size", size);
                return "member/pointHistory";
            }

            Long currentBalance = pointService.getCurrentBalance(currentMember.getId());
            Pageable pageable = PageRequest.of(page, size);
            Page<com.creatorworks.nexus.order.service.PointService.PointHistoryDto> pointHistoryPage = pointService.getPointHistoryDtoList(currentMember.getId(), pageable);
            
            model.addAttribute("currentBalance", currentBalance != null ? currentBalance : 0L);
            model.addAttribute("pointHistoryPage", pointHistoryPage);
            model.addAttribute("page", page);
            model.addAttribute("size", size);

            return "member/pointHistory";
        } catch (Exception e) {
            log.error("포인트 내역 조회 중 오류 발생", e);
            model.addAttribute("currentBalance", 0L);
            model.addAttribute("pointHistoryPage", null);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            return "member/pointHistory";
        }
    }

    private String getEmailFromPrincipal(Object principal) {
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUsername();
        } else if (principal instanceof UserAccount) {
            return ((UserAccount) principal).getUsername();
        } else if (principal instanceof OAuth2User) {
            // OAuth2User의 경우 email 속성을 시도하되, 없으면 getName() 사용
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isEmpty()) {
                return email;
            }
            return oauth2User.getName();
        }
        throw new IllegalStateException("인증된 사용자 정보를 가져올 수 없습니다.");
    }
}