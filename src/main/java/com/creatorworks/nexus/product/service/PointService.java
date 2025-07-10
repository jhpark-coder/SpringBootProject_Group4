package com.creatorworks.nexus.product.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.product.dto.PointChargeRequest;
import com.creatorworks.nexus.product.dto.PointPurchaseRequest;
import com.creatorworks.nexus.product.dto.PointRefundRequest;
import com.creatorworks.nexus.product.dto.PointResponse;
import com.creatorworks.nexus.product.entity.Point;
import com.creatorworks.nexus.product.entity.Point.PointType;
import com.creatorworks.nexus.product.entity.PointRefund;
import com.creatorworks.nexus.product.entity.PointRefund.RefundStatus;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.PointRefundRepository;
import com.creatorworks.nexus.product.repository.PointRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.member.service.IamportRefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PointService {

    private final PointRepository pointRepository;
    private final PointRefundRepository pointRefundRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository; // Order 생성용 추가
    private final IamportRefundService iamportRefundService;

    /**
     * 포인트 충전
     * @param memberId 회원 ID
     * @param request 포인트 충전 요청
     * @return 포인트 충전 결과
     */
    @Transactional
    public PointResponse chargePoint(Long memberId, PointChargeRequest request) {
        // 회원 조회
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
    }

    /**
     * 포인트로 상품 구매
     * @param memberId 회원 ID
     * @param request 포인트 구매 요청
     * @return 포인트 구매 결과
     */
    @Transactional
    public PointResponse purchaseWithPoint(Long memberId, PointPurchaseRequest request) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 현재 포인트 잔액 조회
        Long currentBalance = getCurrentBalance(memberId);

        // 포인트 부족 확인 (0보다 작아지지 않도록 보호)
        if (currentBalance < request.getPrice() || currentBalance < 0) {
            return PointResponse.builder()
                    .success(false)
                    .message("포인트가 부족합니다. 현재 보유 포인트: " + Math.max(0, currentBalance) + "P")
                    .currentBalance(Math.max(0, currentBalance))
                    .build();
        }

        // 포인트 차감 후 잔액이 음수가 되는지 추가 확인
        Long newBalance = currentBalance - request.getPrice();
        if (newBalance < 0) {
            log.warn("포인트 차감 후 잔액이 음수가 됩니다: 회원ID={}, 현재잔액={}, 차감금액={}, 예상잔액={}", 
                    memberId, currentBalance, request.getPrice(), newBalance);
            return PointResponse.builder()
                    .success(false)
                    .message("포인트가 부족합니다. 현재 보유 포인트: " + currentBalance + "P")
                    .currentBalance(currentBalance)
                    .build();
        }

        // 이미 구매한 상품인지 확인
        if (orderRepository.existsByBuyerAndProduct(member, product)) {
            return PointResponse.builder()
                    .success(false)
                    .message("이미 구매한 상품입니다.")
                    .currentBalance(currentBalance)
                    .build();
        }

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

        // Order 엔티티 생성 (구매 이력 기록)
        Order order = Order.builder()
                .buyer(member)
                .product(product)
                .orderDate(LocalDateTime.now())
                .build();

        orderRepository.save(order);

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
    }

    /**
     * 현재 포인트 잔액 조회
     * @param memberId 회원 ID
     * @return 포인트 잔액 (최소 0)
     */
    public Long getCurrentBalance(Long memberId) {
        Long balance = pointRepository.calculateBalanceByMemberId(memberId);
        // 추가 보호: 음수인 경우 0으로 반환
        return Math.max(0, balance);
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

    /**
     * 포인트 추가 (보너스 등)
     * @param memberId 회원 ID
     * @param amount 추가할 포인트
     * @param description 추가 사유
     * @return 포인트 추가 결과
     */
    @Transactional
    public PointResponse addPoints(Long memberId, Long amount, String description) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 현재 포인트 잔액 조회
        Long currentBalance = getCurrentBalance(memberId);
        Long newBalance = currentBalance + amount;

        // 포인트 내역 저장
        Point point = Point.builder()
                .member(member)
                .amount(amount)
                .type(PointType.CHARGE)
                .balanceAfter(newBalance)
                .description(description)
                .merchantUid("bonus_" + UUID.randomUUID().toString().replace("-", ""))
                .build();

        pointRepository.save(point);

        // 회원 포인트 업데이트
        member.setPoint(newBalance.intValue());
        memberRepository.save(member);

        log.info("포인트 추가 완료: 회원ID={}, 추가금액={}, 사유={}, 잔액={}", 
                memberId, amount, description, newBalance);

        return PointResponse.builder()
                .success(true)
                .message("포인트가 추가되었습니다.")
                .currentBalance(newBalance)
                .amount(amount)
                .description(description)
                .transactionDate(LocalDateTime.now())
                .build();
    }

    /**
     * 포인트 환불 요청 (자동 처리)
     * @param memberId 회원 ID
     * @param request 포인트 환불 요청
     * @return 포인트 환불 요청 결과
     */
    @Transactional
    public PointResponse requestRefund(Long memberId, PointRefundRequest request) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 현재 포인트 잔액 조회
        Long currentBalance = getCurrentBalance(memberId);
        
        // 환불 요청 금액이 현재 잔액보다 큰지 확인 (음수 보호)
        if (request.getAmount() > currentBalance || currentBalance < 0) {
            return PointResponse.builder()
                    .success(false)
                    .message("환불 요청 금액이 현재 보유 포인트보다 많습니다. 현재 보유 포인트: " + Math.max(0, currentBalance) + "P")
                    .currentBalance(Math.max(0, currentBalance))
                    .build();
        }

        // 최소 환불 금액 확인 (1,000P)
        if (request.getAmount() < 1000) {
            return PointResponse.builder()
                    .success(false)
                    .message("환불 요청은 최소 1,000P 이상 가능합니다.")
                    .currentBalance(currentBalance)
                    .build();
        }

        // 이미 처리 중인 환불 요청이 있는지 확인
        if (pointRefundRepository.existsByMemberIdAndStatusIn(memberId, 
                java.util.Arrays.asList(RefundStatus.PENDING, RefundStatus.PROCESSING))) {
            return PointResponse.builder()
                    .success(false)
                    .message("이미 처리 중인 환불 요청이 있습니다. 기존 요청이 완료된 후 다시 신청해주세요.")
                    .currentBalance(currentBalance)
                    .build();
        }

        // 환불 UID 생성
        String refundUid = "refund_" + UUID.randomUUID().toString().replace("-", "");

        // 환불 요청 저장 (처리 중 상태로 시작)
        PointRefund refund = PointRefund.builder()
                .member(member)
                .amount(request.getAmount())
                .reason(request.getReason())
                .bankCode(request.getBankCode())
                .accountNumber(request.getAccountNumber())
                .accountHolder(request.getAccountHolder())
                .phoneNumber(request.getPhoneNumber())
                .status(RefundStatus.PROCESSING)
                .refundUid(refundUid)
                .build();

        pointRefundRepository.save(refund);

        // 아임포트를 통한 자동 환불 처리
        try {
            java.util.Map<String, Object> refundResult = iamportRefundService.processRefund(refund);
            
            if ((Boolean) refundResult.get("success")) {
                // 환불 성공 시 포인트 차감
                Long newBalance = currentBalance - request.getAmount();
                
                // 차감 후 잔액이 음수가 되지 않도록 보호
                if (newBalance < 0) {
                    log.warn("환불 처리 후 포인트 잔액이 음수가 됩니다: 회원ID={}, 현재잔액={}, 환불금액={}, 예상잔액={}", 
                            memberId, currentBalance, request.getAmount(), newBalance);
                    newBalance = 0L; // 최소 0으로 설정
                }
                
                // 포인트 내역 저장
                Point point = Point.builder()
                        .member(member)
                        .amount(-request.getAmount()) // 음수로 저장 (환불)
                        .type(PointType.REFUND)
                        .balanceAfter(newBalance)
                        .description("포인트 환불 - " + request.getReason())
                        .merchantUid(refundUid)
                        .build();

                pointRepository.save(point);

                // 회원 포인트 업데이트
                member.setPoint(newBalance.intValue());
                memberRepository.save(member);

                // 환불 상태를 완료로 변경
                refund.setStatus(RefundStatus.COMPLETED);
                refund.setAdminComment("자동 환불 처리 완료");
                pointRefundRepository.save(refund);

                log.info("포인트 자동 환불 완료: 회원ID={}, 환불금액={}, 환불UID={}, 잔액={}", 
                        memberId, request.getAmount(), refundUid, newBalance);

                return PointResponse.builder()
                        .success(true)
                        .message("포인트 환불이 성공적으로 처리되었습니다.")
                        .currentBalance(newBalance)
                        .amount(-request.getAmount())
                        .description("포인트 환불 완료")
                        .transactionDate(LocalDateTime.now())
                        .build();
            } else {
                // 환불 실패 시 상태를 실패로 변경
                refund.setStatus(RefundStatus.FAILED);
                refund.setAdminComment("환불 처리 실패: " + refundResult.get("message"));
                pointRefundRepository.save(refund);

                log.error("포인트 환불 실패: 회원ID={}, 환불금액={}, 환불UID={}, 실패사유={}", 
                        memberId, request.getAmount(), refundUid, refundResult.get("message"));

                return PointResponse.builder()
                        .success(false)
                        .message("환불 처리 중 오류가 발생했습니다: " + refundResult.get("message"))
                        .currentBalance(currentBalance)
                        .build();
            }
        } catch (Exception e) {
            // 예외 발생 시 상태를 실패로 변경
            refund.setStatus(RefundStatus.FAILED);
            refund.setAdminComment("환불 처리 중 시스템 오류: " + e.getMessage());
            pointRefundRepository.save(refund);

            log.error("포인트 환불 처리 중 예외 발생: 회원ID={}, 환불금액={}, 환불UID={}, 오류={}", 
                    memberId, request.getAmount(), refundUid, e.getMessage(), e);

            return PointResponse.builder()
                    .success(false)
                    .message("환불 처리 중 시스템 오류가 발생했습니다.")
                    .currentBalance(currentBalance)
                    .build();
        }
    }

    /**
     * 포인트 환불 요청 목록 조회
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 환불 요청 목록 페이지
     */
    public Page<PointRefund> getRefundHistory(Long memberId, Pageable pageable) {
        return pointRefundRepository.findByMemberIdOrderByRegTimeDesc(memberId, pageable);
    }



    /**
     * 환불 가능한 포인트 금액 조회
     * @param memberId 회원 ID
     * @return 환불 가능한 포인트 금액 (최소 0)
     */
    public Long getRefundableAmount(Long memberId) {
        Long currentBalance = getCurrentBalance(memberId);
        Long pendingRefundAmount = pointRefundRepository.calculateTotalRefundRequestAmount(memberId);
        
        // 음수 보호: 환불 가능 금액이 음수가 되지 않도록
        Long refundableAmount = currentBalance - pendingRefundAmount;
        return Math.max(0, refundableAmount);
    }
} 