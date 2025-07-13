package com.creatorworks.nexus.product.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.entity.Point;
import com.creatorworks.nexus.product.repository.PointRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("pointHistoryService")
@Transactional
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    /**
     * 포인트 충전 내역을 기록합니다.
     */
    @Transactional
    public void addChargeHistory(Member member, Long amount, String description, String impUid, String merchantUid) {
        Long currentBalance = getCurrentBalance(member.getId());
        Long newBalance = currentBalance + amount;

        Point point = Point.builder()
            .member(member)
            .amount(amount)
            .type(Point.PointType.CHARGE)
            .balanceAfter(newBalance)
            .description(description)
            .impUid(impUid)
            .merchantUid(merchantUid)
            .build();
        
        pointRepository.save(point);
        
        log.info("포인트 충전 내역 기록: memberId={}, amount={}, description={}, newBalance={}", 
                member.getId(), amount, description, newBalance);
    }

    /**
     * 포인트 사용 내역을 기록합니다.
     */
    @Transactional
    public void addUseHistory(Member member, Long amount, String description, String merchantUid) {
        Long currentBalance = getCurrentBalance(member.getId());
        Long newBalance = currentBalance - amount;

        if (newBalance < 0) {
            throw new IllegalStateException("포인트 잔액이 부족합니다. 현재 잔액: " + currentBalance + ", 사용 금액: " + amount);
        }

        Point point = Point.builder()
            .member(member)
            .amount(-amount) // 사용은 음수로 기록
            .type(Point.PointType.USE)
            .balanceAfter(newBalance)
            .description(description)
            .merchantUid(merchantUid)
            .build();
        
        pointRepository.save(point);
        
        log.info("포인트 사용 내역 기록: memberId={}, amount={}, description={}, newBalance={}", 
                member.getId(), amount, description, newBalance);
    }

    /**
     * 포인트 환불 내역을 기록합니다.
     */
    @Transactional
    public void addRefundHistory(Member member, Long amount, String reason) {
        Long currentBalance = getCurrentBalance(member.getId());
        Long newBalance = currentBalance + amount;

        Point point = Point.builder()
            .member(member)
            .amount(amount)
            .type(Point.PointType.REFUND)
            .balanceAfter(newBalance)
            .description("포인트 환불: " + reason)
            .merchantUid("refund_" + System.currentTimeMillis())
            .build();
        
        pointRepository.save(point);
        
        log.info("포인트 환불 내역 기록: memberId={}, amount={}, reason={}, newBalance={}", 
                member.getId(), amount, reason, newBalance);
    }

    /**
     * 현재 포인트 잔액을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Long getCurrentBalance(Long memberId) {
        return pointRepository.calculateBalanceByMemberId(memberId);
    }

    /**
     * 포인트 내역을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<Point> getPointHistory(Long memberId, Pageable pageable) {
        return pointRepository.findByMemberIdOrderByRegTimeDesc(memberId, pageable);
    }

    /**
     * 특정 타입의 포인트 내역을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Point> getPointHistoryByType(Long memberId, Point.PointType type) {
        return pointRepository.findByMemberIdAndTypeOrderByRegTimeDesc(memberId, type);
    }

    /**
     * 포인트 내역 DTO
     */
    public static class PointHistoryDto {
        private String description;
        private Long amount;
        private String type;
        private LocalDateTime regTime;
        private Long balanceAfter;
        
        // Getter methods
        public String getDescription() { return description; }
        public Long getAmount() { return amount; }
        public String getType() { return type; }
        public LocalDateTime getRegTime() { return regTime; }
        public Long getBalanceAfter() { return balanceAfter; }
        
        // Setter methods
        public void setDescription(String description) { this.description = description; }
        public void setAmount(Long amount) { this.amount = amount; }
        public void setType(String type) { this.type = type; }
        public void setRegTime(LocalDateTime regTime) { this.regTime = regTime; }
        public void setBalanceAfter(Long balanceAfter) { this.balanceAfter = balanceAfter; }
    }

    /**
     * 포인트 내역을 DTO로 변환하여 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<PointHistoryDto> getPointHistoryDtoList(Long memberId, Pageable pageable) {
        Page<Point> pointPage = pointRepository.findByMemberIdOrderByRegTimeDesc(memberId, pageable);
        
        Page<PointHistoryDto> dtoPage = pointPage.map(point -> {
            PointHistoryDto dto = new PointHistoryDto();
            dto.setDescription(point.getDescription());
            dto.setAmount(point.getAmount());
            dto.setType(point.getType().name());
            dto.setRegTime(point.getRegTime());
            dto.setBalanceAfter(point.getBalanceAfter());
            return dto;
        });
        
        return dtoPage;
    }
} 