package com.creatorworks.nexus.order.service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.dto.TopSellingProductDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class OrderService {

    //20250701 작가 판매량 파악을 위해 추가
    private final OrderRepository orderRepository;

    public List<TopSellingProductDto> getTopSellingProductsThisMonth(Member seller, int limit) {
        // 1. "이번 달"의 시작일과 종료일을 계산합니다.
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime startDate = thisMonth.atDay(1).atStartOfDay(); // 이번 달 1일 00:00
        LocalDateTime endDate = thisMonth.atEndOfMonth().atTime(23, 59, 59); // 이번 달 마지막 날 23:59:59

        // 2. 페이징 정보를 생성합니다 (상위 limit 개만)
        Pageable pageable = PageRequest.of(0, limit);

        // 3. Repository를 호출하여 데이터를 가져옵니다.
        return orderRepository.findTopSellingProductsBySeller(seller, startDate, endDate, pageable);
    }
}