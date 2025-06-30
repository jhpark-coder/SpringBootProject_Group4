package com.creatorworks.nexus.member.controller;

import com.creatorworks.nexus.member.dto.MonthlyCategoryPurchaseDTO;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberOrderRepository;
import com.creatorworks.nexus.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//20250630 차트 생성을 위해 작성됨
@Controller
@RequestMapping(value = "/User")
@RequiredArgsConstructor
public class MyPageController {

    private final MemberOrderRepository memberOrderRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/my-page")
    public String myPage(@AuthenticationPrincipal User user, Model model) {
        // 1. 현재 로그인한 사용자의 ID를 안전하게 가져오기
        Member currentMember = memberRepository.findByEmail(user.getUsername());
            if (currentMember == null) {
                throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
            }
        Long currentMemberId = currentMember.getId();

        // 2. 차트 데이터 조회 기간 설정 (최근 6개월)
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(6).withDayOfMonth(1).toLocalDate().atStartOfDay();

        // 3. DB에서 월별/카테고리별 구매 데이터 조회
        List<MonthlyCategoryPurchaseDTO> purchases = memberOrderRepository.findMonthlyCategoryPurchases(currentMemberId, startDate, endDate);

        // 4. Chart.js 데이터 구조로 가공
        List<String> labels = Stream.iterate(startDate.toLocalDate(), date -> date.plusMonths(1))
                .limit(6)
                .map(date -> date.getMonthValue() + "월")
                .collect(Collectors.toList());

        Map<String, String> categoryColorMap = purchases.stream()
                .collect(Collectors.toMap(
                        MonthlyCategoryPurchaseDTO::categoryName,
                        MonthlyCategoryPurchaseDTO::categoryColor,
                        (existing, replacement) -> existing
                ));

        List<String> categories = new ArrayList<>(categoryColorMap.keySet());
        Collections.sort(categories);

        List<Map<String, Object>> datasets = new ArrayList<>();
        for (String categoryName : categories) {
            Map<String, Object> dataset = new LinkedHashMap<>();
            dataset.put("label", categoryName);

            List<Long> data = new ArrayList<>();
            for (int monthIndex = 0; monthIndex < 6; monthIndex++) {
                LocalDate targetDate = startDate.plusMonths(monthIndex).toLocalDate();

                long count = purchases.stream()
                        .filter(p -> p.categoryName().equals(categoryName) &&
                                p.year() == targetDate.getYear() &&
                                p.month() == targetDate.getMonthValue())
                        .mapToLong(MonthlyCategoryPurchaseDTO::count)
                        .findFirst()
                        .orElse(0L);
                data.add(count);
            }

            dataset.put("data", data);
            dataset.put("backgroundColor", categoryColorMap.get(categoryName));

            datasets.add(dataset);
        }

        // 5. 최종 데이터를 Model에 담아 View로 전달
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartDatasets", datasets);

        return "member/myPage"; // myPage.html 템플릿을 보여줌
    }
}