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

        // 포인트 부족 확인
        if (currentBalance < request.getPrice()) {
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
     * 포인트 환불 요청
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
        
        // 환불 요청 금액이 현재 잔액보다 큰지 확인
        if (request.getAmount() > currentBalance) {
            return PointResponse.builder()
                    .success(false)
                    .message("환불 요청 금액이 현재 보유 포인트보다 많습니다. 현재 보유 포인트: " + currentBalance + "P")
                    .currentBalance(currentBalance)
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

        // 이미 대기중인 환불 요청이 있는지 확인
        if (pointRefundRepository.existsByMemberIdAndStatus(memberId, RefundStatus.PENDING)) {
            return PointResponse.builder()
                    .success(false)
                    .message("이미 대기중인 환불 요청이 있습니다. 기존 요청이 처리된 후 다시 신청해주세요.")
                    .currentBalance(currentBalance)
                    .build();
        }

        // 환불 UID 생성
        String refundUid = "refund_" + UUID.randomUUID().toString().replace("-", "");

        // 환불 요청 저장
        PointRefund refund = PointRefund.builder()
                .member(member)
                .amount(request.getAmount())
                .reason(request.getReason())
                .bankCode(request.getBankCode())
                .accountNumber(request.getAccountNumber())
                .accountHolder(request.getAccountHolder())
                .phoneNumber(request.getPhoneNumber())
                .status(RefundStatus.PENDING)
                .refundUid(refundUid)
                .build();

        pointRefundRepository.save(refund);

        log.info("포인트 환불 요청 완료: 회원ID={}, 요청금액={}, 환불UID={}", 
                memberId, request.getAmount(), refundUid);

        return PointResponse.builder()
                .success(true)
                .message("포인트 환불 요청이 접수되었습니다. 관리자 검토 후 처리됩니다.")
                .currentBalance(currentBalance)
                .amount(request.getAmount())
                .description("포인트 환불 요청")
                .transactionDate(LocalDateTime.now())
                .build();
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
     * 포인트 환불 처리 (관리자용)
     * @param refundId 환불 요청 ID
     * @param approved 승인 여부
     * @param adminComment 관리자 코멘트
     * @return 환불 처리 결과
     */
    @Transactional
    public PointResponse processRefund(Long refundId, boolean approved, String adminComment) {
        PointRefund refund = pointRefundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 요청을 찾을 수 없습니다."));

        if (refund.getStatus() != RefundStatus.PENDING) {
            return PointResponse.builder()
                    .success(false)
                    .message("이미 처리된 환불 요청입니다.")
                    .build();
        }

        if (approved) {
            // 환불 승인 처리
            refund.setStatus(RefundStatus.APPROVED);
            refund.setAdminComment(adminComment);

            // 포인트 차감 및 환불 내역 기록
            Long currentBalance = getCurrentBalance(refund.getMember().getId());
            Long newBalance = currentBalance - refund.getAmount();

            // 포인트 내역 저장
            Point point = Point.builder()
                    .member(refund.getMember())
                    .amount(-refund.getAmount()) // 음수로 저장 (환불)
                    .type(PointType.REFUND)
                    .balanceAfter(newBalance)
                    .description("포인트 환불 - " + refund.getReason())
                    .merchantUid(refund.getRefundUid())
                    .build();

            pointRepository.save(point);

            // 회원 포인트 업데이트
            refund.getMember().setPoint(newBalance.intValue());
            memberRepository.save(refund.getMember());

            log.info("포인트 환불 승인 완료: 환불ID={}, 회원ID={}, 환불금액={}, 잔액={}", 
                    refundId, refund.getMember().getId(), refund.getAmount(), newBalance);

            return PointResponse.builder()
                    .success(true)
                    .message("포인트 환불이 승인되었습니다.")
                    .currentBalance(newBalance)
                    .amount(-refund.getAmount())
                    .description("포인트 환불 승인")
                    .transactionDate(LocalDateTime.now())
                    .build();
        } else {
            // 환불 거절 처리
            refund.setStatus(RefundStatus.REJECTED);
            refund.setAdminComment(adminComment);

            log.info("포인트 환불 거절: 환불ID={}, 회원ID={}, 거절사유={}", 
                    refundId, refund.getMember().getId(), adminComment);

            return PointResponse.builder()
                    .success(true)
                    .message("포인트 환불이 거절되었습니다.")
                    .description("포인트 환불 거절")
                    .transactionDate(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 환불 완료 처리 (관리자용)
     * @param refundId 환불 요청 ID
     * @return 환불 완료 처리 결과
     */
    @Transactional
    public PointResponse completeRefund(Long refundId) {
        PointRefund refund = pointRefundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 요청을 찾을 수 없습니다."));

        if (refund.getStatus() != RefundStatus.APPROVED) {
            return PointResponse.builder()
                    .success(false)
                    .message("승인된 환불 요청만 완료 처리할 수 있습니다.")
                    .build();
        }

        refund.setStatus(RefundStatus.COMPLETED);
        refund.setAdminComment(refund.getAdminComment() + " (환불 완료)");

        log.info("포인트 환불 완료: 환불ID={}, 회원ID={}, 환불금액={}", 
                refundId, refund.getMember().getId(), refund.getAmount());

        return PointResponse.builder()
                .success(true)
                .message("포인트 환불이 완료되었습니다.")
                .description("포인트 환불 완료")
                .transactionDate(LocalDateTime.now())
                .build();
    }

    /**
     * 환불 가능한 포인트 금액 조회
     * @param memberId 회원 ID
     * @return 환불 가능한 포인트 금액
     */
    public Long getRefundableAmount(Long memberId) {
        Long currentBalance = getCurrentBalance(memberId);
        Long pendingRefundAmount = pointRefundRepository.calculateTotalRefundRequestAmount(memberId);
        
        return Math.max(0, currentBalance - pendingRefundAmount);
    }
} 