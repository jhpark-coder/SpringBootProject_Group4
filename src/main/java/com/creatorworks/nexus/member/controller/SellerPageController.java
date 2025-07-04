package com.creatorworks.nexus.member.controller;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.dto.AgeRatioDto;
import com.creatorworks.nexus.order.dto.GenderRatioDto;
import com.creatorworks.nexus.order.dto.MonthlySalesDto;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.service.OrderService;
import com.creatorworks.nexus.order.dto.TopSellingProductDto;
import com.creatorworks.nexus.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerPageController {

    private final MemberRepository memberRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    // ... 다른 서비스/레포지토리

    @GetMapping("/dashboard")
    public String sellerDashboard(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Member seller = memberRepository.findByEmail(principal.getName());

        // --- 1. 월별 판매 현황 데이터 (수정된 로직) ---
        // "11달 전의 1일" 부터 조회하여 현재 달까지 총 12개월을 포함시킴
        LocalDateTime startDate = LocalDateTime.now().minusMonths(11).withDayOfMonth(1).toLocalDate().atStartOfDay();
        List<MonthlySalesDto> rawSalesData = orderRepository.findMonthlySalesBySeller(seller, startDate);

        Map<YearMonth, Long> salesMap = rawSalesData.stream()
                .collect(Collectors.toMap(
                        dto -> YearMonth.of(dto.getYear(), dto.getMonth()),
                        MonthlySalesDto::getSalesCount,
                        Long::sum // 혹시 모를 중복 키에 대비하여 값을 합산
                ));

        // 최종적으로 전달할, 12개월 데이터가 모두 채워진 Map 생성
        Map<String, Long> monthlySalesData = new LinkedHashMap<>();
        // YearMonth.now() 에서 11달 전부터 현재까지 반복
        for (int i = 11; i >= 0; i--) {
            YearMonth targetMonth = YearMonth.now().minusMonths(i);
            String key = targetMonth.toString(); // "YYYY-MM"
            long count = salesMap.getOrDefault(targetMonth, 0L);
            monthlySalesData.put(key, count);
        }

        model.addAttribute("monthlySalesData", monthlySalesData);


        // --- 2. 구매자 성별 비율 데이터 ---
        List<GenderRatioDto> genderRatio = orderRepository.findGenderRatioBySeller(seller);
        model.addAttribute("genderRatioData", genderRatio);

        // --- 3. 구매자 나이대 비율 데이터 ---
        List<AgeRatioDto> ageRatio = orderRepository.findAgeRatioBySeller(seller);
        model.addAttribute("ageRatioData", ageRatio);



        // --- ★★★ 이달의 TOP 4 판매 상품 조회 ★★★ ---
        List<TopSellingProductDto> topSellingProducts = orderService.getTopSellingProductsThisMonth(seller, 4);
        // ★★★★★ 디버깅 로그 추가 ★★★★★
        log.info("조회된 TOP 판매 상품 건수: {}", topSellingProducts.size());
        topSellingProducts.forEach(p ->
                log.info("  - 상품명: {}, 판매량: {}", p.getName(), p.getSalesCount())
        );
        // ★★★★★★★★★★★★★★★★★★★
        model.addAttribute("topSellingProducts", topSellingProducts);
        // ---------------------------------------------

        model.addAttribute("Name", seller.getName());

        return "seller/dashboard"; // sellerDashboard.html 템플릿 반환
    }

    @GetMapping("/my-products")
    public String myProducts() {
        return "seller/myProducts";
    }

    @GetMapping("/inquiry-board")
    public String inquiryBoard() {
        return "seller/inquiryBoard";
    }

    @GetMapping("/review-board")
    public String reviewBoard() {
        return "seller/reviewBoard";
    }
}