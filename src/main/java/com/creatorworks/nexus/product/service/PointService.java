package com.creatorworks.nexus.product.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.dto.PointChargeRequest;
import com.creatorworks.nexus.product.dto.PointPurchaseRequest;
import com.creatorworks.nexus.product.dto.PointResponse;
import com.creatorworks.nexus.product.entity.Point;
import com.creatorworks.nexus.product.entity.Point.PointType;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.PointRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PointService {

    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    /**
     * 포인트 충전
     * @param memberId 회원 ID
     * @param request 포인트 충전 요청
     * @return 포인트 충전 결과
     */
    @Transactional
    public PointResponse chargePoint(Long memberId, PointChargeRequest request) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

            // 중복 충전 방지 (아임포트 UID로 확인)
            if (request.getImpUid() != null && pointRepository.findByImpUid(request.getImpUid()) != null) {
                return PointResponse.builder()
                        .success(false)
                        .message("이미 처리된 결제입니다.")
                        .build();
            }

            // 현재 포인트 잔액 조회
            Long currentBalance = getCurrentBalance(memberId);
            Long newBalance = currentBalance + request.getAmount();

            // 포인트 내역 저장
            Point point = Point.builder()
                    .member(member)
                    .amount(request.getAmount())
                    .type(PointType.CHARGE)
                    .balanceAfter(newBalance)
                    .description("포인트 충전")
                    .impUid(request.getImpUid())
                    .merchantUid(request.getMerchantUid())
                    .build();

            pointRepository.save(point);

            // 회원 포인트 업데이트
            member.setPoint(newBalance.intValue());
            memberRepository.save(member);

            log.info("포인트 충전 완료: 회원ID={}, 충전금액={}, 잔액={}", 
                    memberId, request.getAmount(), newBalance);

            return PointResponse.builder()
                    .success(true)
                    .message("포인트 충전이 완료되었습니다.")
                    .currentBalance(newBalance)
                    .amount(request.getAmount())
                    .description("포인트 충전")
                    .transactionDate(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("포인트 충전 실패: 회원ID={}, 오류={}", memberId, e.getMessage());
            return PointResponse.builder()
                    .success(false)
                    .message("포인트 충전 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 포인트로 상품 구매
     * @param memberId 회원 ID
     * @param request 포인트 구매 요청
     * @return 포인트 구매 결과
     */
    @Transactional
    public PointResponse purchaseWithPoint(Long memberId, PointPurchaseRequest request) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            // 현재 포인트 잔액 조회
            Long currentBalance = getCurrentBalance(memberId);

            // 포인트 부족 확인
            if (currentBalance < request.getPrice()) {
                return PointResponse.builder()
                        .success(false)
                        .message("포인트가 부족합니다. 현재 보유 포인트: " + currentBalance + "P")
                        .currentBalance(currentBalance)
                        .build();
            }

            // 포인트 차감
            Long newBalance = currentBalance - request.getPrice();

            // 포인트 내역 저장
            Point point = Point.builder()
                    .member(member)
                    .product(product)
                    .amount(-request.getPrice()) // 음수로 저장 (사용)
                    .type(PointType.USE)
                    .balanceAfter(newBalance)
                    .description(product.getName() + " 구매")
                    .merchantUid("purchase_" + UUID.randomUUID().toString().replace("-", ""))
                    .build();

            pointRepository.save(point);

            // 회원 포인트 업데이트
            member.setPoint(newBalance.intValue());
            memberRepository.save(member);

            log.info("포인트 구매 완료: 회원ID={}, 상품ID={}, 사용포인트={}, 잔액={}", 
                    memberId, request.getProductId(), request.getPrice(), newBalance);

            return PointResponse.builder()
                    .success(true)
                    .message("포인트 구매가 완료되었습니다.")
                    .currentBalance(newBalance)
                    .amount(-request.getPrice())
                    .description(product.getName() + " 구매")
                    .transactionDate(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("포인트 구매 실패: 회원ID={}, 상품ID={}, 오류={}", 
                    memberId, request.getProductId(), e.getMessage());
            return PointResponse.builder()
                    .success(false)
                    .message("포인트 구매 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 현재 포인트 잔액 조회
     * @param memberId 회원 ID
     * @return 포인트 잔액
     */
    public Long getCurrentBalance(Long memberId) {
        return pointRepository.calculateBalanceByMemberId(memberId);
    }

    /**
     * 포인트 내역 조회
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
     */
    public Page<Point> getPointHistory(Long memberId, Pageable pageable) {
        return pointRepository.findByMemberIdOrderByRegTimeDesc(memberId, pageable);
    }

    /**
     * 포인트 충전 가능 여부 확인
     * @param memberId 회원 ID
     * @param amount 충전할 금액
     * @return 충전 가능 여부
     */
    public boolean canCharge(Long memberId, Long amount) {
        if (amount <= 0) {
            return false;
        }
        
        Member member = memberRepository.findById(memberId).orElse(null);
        return member != null;
    }

    /**
     * 포인트 사용 가능 여부 확인
     * @param memberId 회원 ID
     * @param amount 사용할 금액
     * @return 사용 가능 여부
     */
    public boolean canUse(Long memberId, Long amount) {
        if (amount <= 0) {
            return false;
        }
        
        Long currentBalance = getCurrentBalance(memberId);
        return currentBalance >= amount;
    }
} 