package com.creatorworks.nexus.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.dto.RefundRequest;
import com.creatorworks.nexus.order.dto.RefundResponse;
import com.creatorworks.nexus.order.dto.RefundStatisticsDto;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Payment;
import com.creatorworks.nexus.order.entity.Refund;
import com.creatorworks.nexus.order.entity.Refund.RefundStatus;
import com.creatorworks.nexus.order.entity.Refund.RefundType;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.repository.PaymentRepository;
import com.creatorworks.nexus.order.repository.RefundRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final IamportRefundService iamportRefundService;
    private final PointService pointService;

    /**
     * 환불 요청을 생성합니다.
     */
    public Refund createRefundRequest(Long memberId, RefundRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        // 환불 타입 결정
        RefundType refundType = determineRefundType(request);
        
        // 중복 요청 체크
        validateRefundRequest(memberId, request);
        
        // 환불 가능 여부 검증
        validateRefundEligibility(memberId, request);

        // 원본 주문/결제 정보 조회
        Order order = null;
        Payment payment = null;
        String originalImpUid = null;
        String originalMerchantUid = null;
        Long originalAmount = null;

        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + request.getOrderId()));
            payment = order.getPayment();
            if (payment != null) {
                originalImpUid = payment.getImpUid();
                originalMerchantUid = payment.getMerchantUid();
                originalAmount = payment.getAmount();
            }
        } else if (request.getPaymentId() != null) {
            payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + request.getPaymentId()));
            order = payment.getOrder();
            originalImpUid = payment.getImpUid();
            originalMerchantUid = payment.getMerchantUid();
            originalAmount = payment.getAmount();
        } else if (request.getOriginalImpUid() != null) {
            originalImpUid = request.getOriginalImpUid();
            // 아임포트에서 원본 결제 정보 조회
            Map<String, Object> paymentInfo = iamportRefundService.getOriginalPaymentInfo(originalImpUid);
            if (paymentInfo.containsKey("amount")) {
                originalAmount = Long.valueOf(paymentInfo.get("amount").toString());
            }
        }

        // 환불 엔티티 생성
        Refund refund = Refund.builder()
                .member(member)
                .order(order)
                .payment(payment)
                .refundType(refundType)
                .refundStatus(RefundStatus.PENDING)
                .amount(request.getAmount())
                .reason(request.getReason())
                .bankCode(request.getBankCode())
                .accountNumber(request.getAccountNumber())
                .accountHolder(request.getAccountHolder())
                .phoneNumber(request.getPhoneNumber())
                .originalImpUid(originalImpUid)
                .originalMerchantUid(originalMerchantUid)
                .originalAmount(originalAmount)
                .build();

        refund = refundRepository.save(refund);
        
        log.info("환불 요청 생성: 환불ID={}, 회원ID={}, 금액={}, 타입={}", 
                refund.getId(), memberId, request.getAmount(), refundType);

        return refund;
    }

    /**
     * 환불 요청 생성 (컨트롤러용)
     */
    public RefundResponse createRefundRequest(RefundRequest request, Long memberId) {
        try {
            Refund refund = createRefundRequest(memberId, request);
            return RefundResponse.builder()
                    .success(true)
                    .refundId(refund.getId())
                    .message("환불 요청이 성공적으로 등록되었습니다.")
                    .build();
        } catch (Exception e) {
            log.error("환불 요청 생성 오류: memberId={}, orderId={}, 오류={}", 
                    memberId, request.getOrderId(), e.getMessage(), e);
            return RefundResponse.builder()
                    .success(false)
                    .message("환불 요청 생성 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 내 환불 내역 조회
     */
    public List<Refund> getMyRefunds(Long memberId, int page, int size) {
        return refundRepository.findByMemberIdOrderByRegTimeDesc(memberId, 
                PageRequest.of(page, size)).getContent();
    }

    /**
     * 내 환불 내역 개수 조회
     */
    public long getMyRefundsCount(Long memberId) {
        return refundRepository.countByMemberId(memberId);
    }

    /**
     * 환불 상세 조회 (본인 확인)
     */
    public Refund getRefundDetail(Long refundId, Long memberId) {
        return refundRepository.findByIdAndMemberId(refundId, memberId);
    }

    /**
     * 환불 요청 취소
     */
    public RefundResponse cancelRefundRequest(Long refundId, Long memberId) {
        try {
            Refund refund = refundRepository.findByIdAndMemberId(refundId, memberId);
            
            if (refund == null) {
                return RefundResponse.builder()
                        .success(false)
                        .message("환불 정보를 찾을 수 없습니다.")
                        .build();
            }
            
            if (refund.getRefundStatus() != RefundStatus.PENDING) {
                return RefundResponse.builder()
                        .success(false)
                        .message("처리 중이거나 완료된 환불은 취소할 수 없습니다.")
                        .build();
            }
            
            refund.cancel();
            refundRepository.save(refund);
            
            return RefundResponse.builder()
                    .success(true)
                    .refundId(refund.getId())
                    .message("환불 요청이 취소되었습니다.")
                    .build();
                    
        } catch (Exception e) {
            log.error("환불 요청 취소 오류: refundId={}, memberId={}, 오류={}", 
                    refundId, memberId, e.getMessage(), e);
            return RefundResponse.builder()
                    .success(false)
                    .message("환불 요청 취소 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 환불 처리를 시작합니다.
     */
    public Refund processRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 요청을 찾을 수 없습니다: " + refundId));

        if (refund.getRefundStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException("대기중인 환불 요청만 처리할 수 있습니다.");
        }

        // 처리 시작
        refund.startProcessing();
        refund = refundRepository.save(refund);

        try {
            // 환불 타입에 따른 처리
            if (refund.getRefundType() == RefundType.POINT_REFUND) {
                // 포인트 환불 처리
                processPointRefund(refund);
            } else {
                // 아임포트를 통한 실제 환불 처리
                Map<String, Object> result = iamportRefundService.processRefund(refund);
                
                if ((Boolean) result.get("success")) {
                    // 환불 성공
                    refund.complete();
                    refund.setRefundUid((String) result.get("refundUid"));
                    refund.setRefundDate(LocalDateTime.now());
                    
                    log.info("환불 처리 완료: 환불ID={}, 환불UID={}, 금액={}", 
                            refundId, result.get("refundUid"), refund.getAmount());
                } else {
                    // 환불 실패
                    refund.fail((String) result.get("message"));
                    log.error("환불 처리 실패: 환불ID={}, 사유={}", refundId, result.get("message"));
                }
            }
        } catch (Exception e) {
            // 예외 발생 시 실패 처리
            refund.fail("환불 처리 중 오류가 발생했습니다: " + e.getMessage());
            log.error("환불 처리 중 예외 발생: 환불ID={}, 오류={}", refundId, e.getMessage(), e);
        }

        return refundRepository.save(refund);
    }

    /**
     * 환불 재처리를 시작합니다 (실패한 환불에 대해).
     */
    public Refund retryRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 요청을 찾을 수 없습니다: " + refundId));

        if (refund.getRefundStatus() != RefundStatus.FAILED) {
            throw new IllegalStateException("실패한 환불 요청만 재처리할 수 있습니다.");
        }

        // 재처리 시작
        refund.startProcessing();
        refund.setAdminComment("관리자에 의한 재처리");
        refund = refundRepository.save(refund);

        return processRefund(refundId);
    }

    /**
     * 환불 요청을 취소합니다.
     */
    public Refund cancelRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 요청을 찾을 수 없습니다: " + refundId));

        if (refund.getRefundStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException("대기중인 환불 요청만 취소할 수 있습니다.");
        }

        refund.cancel();
        return refundRepository.save(refund);
    }

    /**
     * 회원의 환불 이력을 조회합니다.
     */
    public Page<Refund> getRefundHistory(Long memberId, Pageable pageable) {
        return refundRepository.findByMemberIdOrderByRegTimeDesc(memberId, pageable);
    }

    /**
     * 환불 상세 정보를 조회합니다.
     */
    public Refund getRefundDetail(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 요청을 찾을 수 없습니다: " + refundId));
    }

    /**
     * 상태별 환불 목록을 조회합니다 (관리자용).
     */
    public Page<Refund> getRefundsByStatus(RefundStatus status, Pageable pageable) {
        return refundRepository.findByRefundStatusOrderByRegTimeDesc(status, pageable);
    }

    /**
     * 타입별 환불 목록을 조회합니다 (관리자용).
     */
    public Page<Refund> getRefundsByType(RefundType type, Pageable pageable) {
        return refundRepository.findByRefundTypeOrderByRegTimeDesc(type, pageable);
    }

    /**
     * 최근 환불 목록을 조회합니다 (관리자용).
     */
    public Page<Refund> getRecentRefunds(Pageable pageable) {
        return refundRepository.findRecentRefunds(pageable);
    }

    /**
     * 환불 상태와 타입별 조회
     */
    public Page<Refund> getRefundsByStatusAndType(RefundStatus status, RefundType type, Pageable pageable) {
        return refundRepository.findByRefundStatusAndRefundTypeOrderByRegTimeDesc(status, type, pageable);
    }

    /**
     * 환불 ID로 조회
     */
    public Refund getRefundById(Long refundId) {
        return refundRepository.findById(refundId).orElse(null);
    }

    /**
     * 관리자 코멘트 추가
     */
    public Refund addAdminComment(Long refundId, String comment) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 요청을 찾을 수 없습니다: " + refundId));
        
        refund.setAdminComment(comment);
        return refundRepository.save(refund);
    }

    /**
     * 환불 통계를 조회합니다 (관리자용).
     */
    public RefundStatisticsDto getRefundStatistics() {
        long totalRefunds = refundRepository.count();
        long pendingRefunds = refundRepository.countByRefundStatus(RefundStatus.PENDING);
        long processingRefunds = refundRepository.countByRefundStatus(RefundStatus.PROCESSING);
        long completedRefunds = refundRepository.countByRefundStatus(RefundStatus.COMPLETED);
        long failedRefunds = refundRepository.countByRefundStatus(RefundStatus.FAILED);
        long cancelledRefunds = refundRepository.countByRefundStatus(RefundStatus.CANCELLED);

        long pointRefunds = refundRepository.countByRefundType(RefundType.POINT_REFUND);
        long paymentRefunds = refundRepository.countByRefundType(RefundType.PAYMENT_REFUND);
        long subscriptionCancels = refundRepository.countByRefundType(RefundType.SUBSCRIPTION_CANCEL);

        return RefundStatisticsDto.builder()
                .totalRefunds(totalRefunds)
                .pendingRefunds(pendingRefunds)
                .processingRefunds(processingRefunds)
                .completedRefunds(completedRefunds)
                .failedRefunds(failedRefunds)
                .cancelledRefunds(cancelledRefunds)
                .pointRefunds(pointRefunds)
                .paymentRefunds(paymentRefunds)
                .subscriptionCancels(subscriptionCancels)
                .build();
    }

    /**
     * 회원의 총 환불 요청 금액을 조회합니다.
     */
    public Long getTotalRefundRequestAmount(Long memberId) {
        return refundRepository.calculateTotalRefundRequestAmount(memberId);
    }

    /**
     * 회원의 완료된 환불 총액을 조회합니다.
     */
    public Long getTotalCompletedRefundAmount(Long memberId) {
        return refundRepository.calculateTotalCompletedRefundAmount(memberId);
    }

    /**
     * 환불 타입을 결정합니다.
     */
    private RefundType determineRefundType(RefundRequest request) {
        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId()).orElse(null);
            if (order != null) {
                switch (order.getOrderType()) {
                    case POINT_PURCHASE:
                        return RefundType.POINT_REFUND;
                    case SUBSCRIPTION:
                        return RefundType.SUBSCRIPTION_CANCEL;
                    case PRODUCT_PURCHASE:
                        return RefundType.PAYMENT_REFUND;
                }
            }
        }
        
        if (request.getPaymentId() != null) {
            Payment payment = paymentRepository.findById(request.getPaymentId()).orElse(null);
            if (payment != null) {
                switch (payment.getPaymentType()) {
                    case POINT:
                        return RefundType.POINT_REFUND;
                    case CARD:
                    case BANK_TRANSFER:
                        return RefundType.PAYMENT_REFUND;
                }
            }
        }
        
        // 기본값은 포인트 환불
        return RefundType.POINT_REFUND;
    }

    /**
     * 환불 요청 유효성을 검증합니다.
     */
    private void validateRefundRequest(Long memberId, RefundRequest request) {
        // 최소 금액 검증
        if (request.getAmount() < 1000) {
            throw new IllegalArgumentException("환불 요청 금액은 최소 1,000원 이상이어야 합니다.");
        }

        // 최대 금액 검증
        if (request.getAmount() > 1000000) {
            throw new IllegalArgumentException("환불 요청 금액은 최대 1,000,000원까지 가능합니다.");
        }

        // 중복 요청 체크 (대기중/처리중인 환불이 있는지)
        List<RefundStatus> activeStatuses = List.of(RefundStatus.PENDING, RefundStatus.PROCESSING);
        if (refundRepository.existsByMemberIdAndRefundStatusIn(memberId, activeStatuses)) {
            throw new IllegalStateException("이미 처리 중인 환불 요청이 있습니다.");
        }
    }

    /**
     * 환불 가능 여부를 검증합니다.
     */
    private void validateRefundEligibility(Long memberId, RefundRequest request) {
        // 주문 ID가 있는 경우 상세 검증
        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + request.getOrderId()));
            
            // 본인의 주문인지 확인
            if (!order.getBuyer().getId().equals(memberId)) {
                throw new IllegalArgumentException("본인의 주문만 환불할 수 있습니다.");
            }
            
            // 포인트로 구매한 상품의 경우 환불 조건 검증
            if (order.getOrderType() == Order.OrderType.PRODUCT_PURCHASE && 
                order.getPayment() != null && 
                order.getPayment().getPaymentType() == Payment.PaymentType.POINT) {
                
                // 이미 읽은 상품은 환불 불가
                if (order.isRead()) {
                    throw new IllegalArgumentException("이미 확인한 상품은 환불할 수 없습니다.");
                }
                
                // 환불 금액이 구매 금액과 일치하는지 확인
                if (!request.getAmount().equals(order.getTotalAmount())) {
                    throw new IllegalArgumentException("포인트 구매 상품의 환불은 구매 금액과 동일해야 합니다.");
                }
            }
        }
        
        // 포인트 환불의 경우 포인트 잔액 확인
        if (request.getOrderId() == null && request.getPaymentId() == null) {
            // 포인트 환불인 경우 추가 검증 로직 필요
            // PointService에서 포인트 잔액 확인
        }
        
        // 결제 환불의 경우 원본 결제 정보 확인
        if (request.getOriginalImpUid() != null) {
            Map<String, Object> paymentInfo = iamportRefundService.getOriginalPaymentInfo(request.getOriginalImpUid());
            if (!(Boolean) paymentInfo.get("success")) {
                throw new IllegalArgumentException("원본 결제 정보를 찾을 수 없습니다.");
            }
        }
    }

    /**
     * 포인트 환불 처리를 수행합니다.
     */
    private void processPointRefund(Refund refund) {
        try {
            // 포인트 환불 로직
            Member member = refund.getMember();
            Long refundAmount = refund.getAmount();

            // 포인트 잔액 확인
            Long currentPoint = pointService.getCurrentBalance(member.getId());
            if (currentPoint < refundAmount) {
                throw new IllegalStateException("포인트 잔액이 환불 금액보다 적어 환불할 수 없습니다.");
            }

            // 포인트 차감 (기존 포인트에서 차감)
            member.setPoint((int) (currentPoint - refundAmount));
            memberRepository.save(member);

            // 환불 완료 처리
            refund.complete();
            refund.setRefundUid("point_refund_" + System.currentTimeMillis()); // 임시 UID
            refund.setRefundDate(LocalDateTime.now());

            log.info("포인트 환불 처리 완료: 환불ID={}, 환불UID={}, 금액={}", 
                    refund.getId(), refund.getRefundUid(), refund.getAmount());

        } catch (Exception e) {
            refund.fail("포인트 환불 처리 중 오류가 발생했습니다: " + e.getMessage());
            log.error("포인트 환불 처리 중 예외 발생: 환불ID={}, 오류={}", refund.getId(), e.getMessage(), e);
        }
    }
} 