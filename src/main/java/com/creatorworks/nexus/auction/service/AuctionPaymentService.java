package com.creatorworks.nexus.auction.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.auction.dto.AuctionPaymentRequest;
import com.creatorworks.nexus.auction.dto.AuctionPaymentResponse;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.AuctionPayment;
import com.creatorworks.nexus.auction.entity.PaymentStatus;
import com.creatorworks.nexus.auction.repository.AuctionPaymentRepository;
import com.creatorworks.nexus.auction.repository.AuctionRepository;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.notification.dto.PaymentNotificationRequest;
import com.creatorworks.nexus.notification.entity.NotificationCategory;
import com.creatorworks.nexus.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuctionPaymentService {

    private final AuctionPaymentRepository auctionPaymentRepository;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    // private final IamportService iamportService; // 임시 주석 처리

    /**
     * 경매 결제 처리
     */
    public AuctionPaymentResponse processPayment(AuctionPaymentRequest request, String memberEmail) {
        // 경매 정보 조회
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));

        // 입찰자 정보 조회
        Member bidder = memberRepository.findByEmail(memberEmail);
        if (bidder == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 아임포트 결제 검증 (임시로 테스트 모드)
        // boolean isValidPayment = iamportService.verifyPayment(request.getImpUid(), request.getMerchantUid(), request.getAmount());
        boolean isValidPayment = true; // 임시로 항상 성공

        if (!isValidPayment) {
            throw new IllegalArgumentException("결제 검증에 실패했습니다.");
        }

        // 결제 정보 저장
        AuctionPayment payment = AuctionPayment.builder()
                .auction(auction)
                .bidder(bidder)
                .amount(request.getAmount())
                .impUid(request.getImpUid())
                .merchantUid(request.getMerchantUid())
                .cardNumber(request.getCardNumber())
                .cardType(request.getCardType())
                .status(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();

        AuctionPayment savedPayment = auctionPaymentRepository.save(payment);

        // 경매 결제 성공 알림 전송
        sendAuctionPaymentSuccessNotification(savedPayment);

        return convertToResponse(savedPayment);
    }

    /**
     * 결제 실패 처리
     */
    public void processPaymentFailure(String impUid, String failureReason) {
        Optional<AuctionPayment> paymentOpt = auctionPaymentRepository.findByImpUid(impUid);

        if (paymentOpt.isPresent()) {
            AuctionPayment payment = paymentOpt.get();
            payment.fail(failureReason);
            auctionPaymentRepository.save(payment);
            
            // 경매 결제 실패 알림 전송
            sendAuctionPaymentFailureNotification(payment, failureReason);
        }
    }

    /**
     * 결제 취소 처리
     */
    public void cancelPayment(String impUid) {
        Optional<AuctionPayment> paymentOpt = auctionPaymentRepository.findByImpUid(impUid);

        if (paymentOpt.isPresent()) {
            AuctionPayment payment = paymentOpt.get();
            payment.cancel();
            auctionPaymentRepository.save(payment);

            // 경매 결제 취소 알림 전송
            sendAuctionPaymentCancellationNotification(payment);

            // 아임포트 결제 취소
            // iamportService.cancelPayment(impUid, "사용자 요청");
        }
    }

    /**
     * 경매 결제 성공 알림 전송
     */
    private void sendAuctionPaymentSuccessNotification(AuctionPayment payment) {
        try {
            String message = String.format("경매 결제가 성공적으로 완료되었습니다. 경매: %s, 금액: %,d원", 
                payment.getAuction().getName(), payment.getAmount());
            
            PaymentNotificationRequest notificationDto = new PaymentNotificationRequest();
            notificationDto.setTargetUserId(payment.getBidder().getId());
            notificationDto.setMessage(message);
            notificationDto.setType("auction_payment_success");
            notificationDto.setCategory(NotificationCategory.AUCTION);
            notificationDto.setLink("/api/auction/payments/success?impUid=" + payment.getImpUid());
            notificationDto.setAmount(payment.getAmount());
            notificationDto.setPaymentMethod("신용카드");
            notificationDto.setOrderId(payment.getAuction().getId().toString());
            
            // 실시간 알림 전송
            notificationService.sendPaymentNotification(notificationDto);
            
            // DB에 알림 저장
            notificationService.savePaymentNotification(notificationDto, 
                "/api/auction/payments/success?impUid=" + payment.getImpUid());
            
            log.info("경매 결제 성공 알림 전송 완료: userId={}, auctionId={}, amount={}", 
                payment.getBidder().getId(), payment.getAuction().getId(), payment.getAmount());
        } catch (Exception e) {
            log.error("경매 결제 성공 알림 전송 실패: paymentId={}, error={}", payment.getId(), e.getMessage());
        }
    }

    /**
     * 경매 결제 실패 알림 전송
     */
    private void sendAuctionPaymentFailureNotification(AuctionPayment payment, String failureReason) {
        try {
            String message = String.format("경매 결제가 실패했습니다. 경매: %s, 금액: %,d원, 사유: %s", 
                payment.getAuction().getName(), payment.getAmount(), failureReason);
            
            PaymentNotificationRequest notificationDto = new PaymentNotificationRequest();
            notificationDto.setTargetUserId(payment.getBidder().getId());
            notificationDto.setMessage(message);
            notificationDto.setType("auction_payment_failed");
            notificationDto.setCategory(NotificationCategory.AUCTION);
            notificationDto.setLink("/api/auction/payments/fail?message=" + failureReason);
            notificationDto.setAmount(payment.getAmount());
            notificationDto.setPaymentMethod("신용카드");
            notificationDto.setOrderId(payment.getAuction().getId().toString());
            
            // 실시간 알림 전송
            notificationService.sendPaymentNotification(notificationDto);
            
            // DB에 알림 저장
            notificationService.savePaymentNotification(notificationDto, 
                "/api/auction/payments/fail?message=" + failureReason);
            
            log.info("경매 결제 실패 알림 전송 완료: userId={}, auctionId={}, amount={}, reason={}", 
                payment.getBidder().getId(), payment.getAuction().getId(), payment.getAmount(), failureReason);
        } catch (Exception e) {
            log.error("경매 결제 실패 알림 전송 실패: paymentId={}, error={}", payment.getId(), e.getMessage());
        }
    }

    /**
     * 경매 결제 취소 알림 전송
     */
    private void sendAuctionPaymentCancellationNotification(AuctionPayment payment) {
        try {
            String message = String.format("경매 결제가 취소되었습니다. 경매: %s, 금액: %,d원", 
                payment.getAuction().getName(), payment.getAmount());
            
            PaymentNotificationRequest notificationDto = new PaymentNotificationRequest();
            notificationDto.setTargetUserId(payment.getBidder().getId());
            notificationDto.setMessage(message);
            notificationDto.setType("auction_payment_cancelled");
            notificationDto.setCategory(NotificationCategory.AUCTION);
            notificationDto.setLink("/api/auction/payments/cancel?impUid=" + payment.getImpUid());
            notificationDto.setAmount(payment.getAmount());
            notificationDto.setPaymentMethod("신용카드");
            notificationDto.setOrderId(payment.getAuction().getId().toString());
            
            // 실시간 알림 전송
            notificationService.sendPaymentNotification(notificationDto);
            
            // DB에 알림 저장
            notificationService.savePaymentNotification(notificationDto, 
                "/api/auction/payments/cancel?impUid=" + payment.getImpUid());
            
            log.info("경매 결제 취소 알림 전송 완료: userId={}, auctionId={}, amount={}", 
                payment.getBidder().getId(), payment.getAuction().getId(), payment.getAmount());
        } catch (Exception e) {
            log.error("경매 결제 취소 알림 전송 실패: paymentId={}, error={}", payment.getId(), e.getMessage());
        }
    }

    /**
     * 사용자별 결제 내역 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<AuctionPaymentResponse> getUserPaymentHistory(String memberEmail, Pageable pageable) {
        Member member = memberRepository.findByEmail(memberEmail);
        if (member == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Page<AuctionPayment> payments = auctionPaymentRepository.findByBidderOrderByPaymentDateDesc(member, pageable);

        return payments.map(this::convertToResponse);
    }

    /**
     * 경매별 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<AuctionPaymentResponse> getAuctionPaymentHistory(Long auctionId) {
        List<AuctionPayment> payments = auctionPaymentRepository.findByAuctionIdOrderByPaymentDateDesc(auctionId);

        return payments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 결제 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public AuctionPaymentResponse getPaymentDetail(Long paymentId) {
        AuctionPayment payment = auctionPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        return convertToResponse(payment);
    }

    /**
     * 사용자별 결제 통계
     */
    @Transactional(readOnly = true)
    public PaymentStatistics getUserPaymentStatistics(String memberEmail) {
        Member member = memberRepository.findByEmail(memberEmail);
        if (member == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        long totalPayments = auctionPaymentRepository.countByBidderAndStatus(member, PaymentStatus.SUCCESS);
        long failedPayments = auctionPaymentRepository.countByBidderAndStatus(member, PaymentStatus.FAILED);
        Long totalAmount = auctionPaymentRepository.getTotalPaymentAmountByBidder(member);

        return PaymentStatistics.builder()
                .totalPayments(totalPayments)
                .failedPayments(failedPayments)
                .totalAmount(totalAmount != null ? totalAmount : 0L)
                .build();
    }

    /**
     * DTO 변환
     */
    private AuctionPaymentResponse convertToResponse(AuctionPayment payment) {
        AuctionPaymentResponse response = new AuctionPaymentResponse();
        response.setId(payment.getId());
        response.setAuctionId(payment.getAuction().getId());
        response.setAuctionTitle(payment.getAuction().getName());
        response.setBidderName(payment.getBidder().getName());
        response.setAmount(payment.getAmount());
        response.setImpUid(payment.getImpUid());
        response.setMerchantUid(payment.getMerchantUid());
        response.setCardNumber(payment.getCardNumber());
        response.setCardType(payment.getCardType());
        response.setStatus(payment.getStatus());
        response.setPaymentDate(payment.getPaymentDate());
        response.setFailureReason(payment.getFailureReason());

        return response;
    }

    /**
     * 결제 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PaymentStatistics {
        private long totalPayments;
        private long failedPayments;
        private long totalAmount;
    }
}