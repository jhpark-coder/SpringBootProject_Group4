package com.creatorworks.nexus.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.notification.dto.PaymentNotificationRequest;
import com.creatorworks.nexus.notification.entity.NotificationCategory;
import com.creatorworks.nexus.notification.service.NotificationService;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Payment;
import com.creatorworks.nexus.order.entity.Payment.PaymentStatus;
import com.creatorworks.nexus.order.entity.Payment.PaymentType;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    /**
     * 결제 정보를 생성합니다.
     */
    public Payment createPayment(Order order, PaymentType paymentType, Long amount,
                               String impUid, String merchantUid, String customerUid,
                               String cardNumber, String cardType) {
        
        // 중복 결제 방지
        if (impUid != null && paymentRepository.existsByImpUid(impUid)) {
            throw new IllegalArgumentException("이미 처리된 결제입니다: " + impUid);
        }
        
        // 중복 주문 방지
        if (merchantUid != null && paymentRepository.existsByMerchantUid(merchantUid)) {
            throw new IllegalArgumentException("이미 존재하는 주문입니다: " + merchantUid);
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentType(paymentType)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(amount)
                .impUid(impUid)
                .merchantUid(merchantUid)
                .customerUid(customerUid)
                .cardNumber(cardNumber)
                .cardType(cardType)
                .build();

        return paymentRepository.save(payment);
    }

    /**
     * 결제를 완료 처리합니다.
     */
    public Payment completePayment(String impUid) {
        Payment payment = paymentRepository.findByImpUid(impUid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + impUid));

        payment.complete();
        payment.getOrder().complete();
        paymentRepository.save(payment);
        
        // 포인트 충전이 아닌 경우에만 결제 성공 알림 전송
        if (payment.getOrder().getOrderType() != Order.OrderType.POINT_PURCHASE) {
            sendPaymentSuccessNotification(payment);
        }

        log.info("결제 완료: impUid={}, amount={}", impUid, payment.getAmount());
        return payment;
    }

    /**
     * 결제를 실패 처리합니다.
     */
    public Payment failPayment(String impUid, String failureReason) {
        Payment payment = paymentRepository.findByImpUid(impUid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + impUid));

        payment.fail(failureReason);
        payment.getOrder().fail();
        paymentRepository.save(payment);
        
        // 결제 실패 알림 전송
        sendPaymentFailureNotification(payment, failureReason);
        
        log.warn("결제 실패: impUid={}, reason={}", impUid, failureReason);
        return payment;
    }

    /**
     * 결제를 취소 처리합니다.
     */
    public Payment cancelPayment(String impUid) {
        Payment payment = paymentRepository.findByImpUid(impUid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + impUid));

        payment.cancel();
        payment.getOrder().cancel();
        paymentRepository.save(payment);
        
        // 결제 취소 알림 전송
        sendPaymentCancellationNotification(payment);
        
        log.info("결제 취소: impUid={}", impUid);
        return payment;
    }

    /**
     * impUid로 결제 정보를 조회합니다.
     */
    public Optional<Payment> findByImpUid(String impUid) {
        return paymentRepository.findByImpUid(impUid);
    }

    /**
     * merchantUid로 결제 정보를 조회합니다.
     */
    public Optional<Payment> findByMerchantUid(String merchantUid) {
        return paymentRepository.findByMerchantUid(merchantUid);
    }

    /**
     * customerUid로 정기결제 정보를 조회합니다.
     */
    public Optional<Payment> findByCustomerUid(String customerUid) {
        return paymentRepository.findByCustomerUid(customerUid);
    }

    /**
     * 다음 결제일이 임박한 정기결제를 조회합니다.
     */
    public List<Payment> findUpcomingRecurringPayments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);
        return paymentRepository.findUpcomingRecurringPayments(now, threeDaysLater);
    }

    /**
     * 결제 실패한 정기결제를 조회합니다.
     */
    public List<Payment> findFailedRecurringPayments() {
        return paymentRepository.findFailedRecurringPayments();
    }

    /**
     * 정기결제의 다음 결제일을 설정합니다.
     */
    public void setNextBillingDate(String customerUid, LocalDateTime nextBillingDate) {
        Payment payment = paymentRepository.findByCustomerUid(customerUid)
                .orElseThrow(() -> new IllegalArgumentException("정기결제 정보를 찾을 수 없습니다: " + customerUid));
        
        payment.setNextBillingDate(nextBillingDate);
        paymentRepository.save(payment);
    }

    /**
     * 포인트 결제를 처리합니다.
     */
    public Payment processPointPayment(Order order, Long amount, String merchantUid) {
        return createPayment(order, PaymentType.POINT, amount, null, merchantUid, null, null, null);
    }

    /**
     * 카드 결제를 처리합니다.
     */
    public Payment processCardPayment(Order order, Long amount, String impUid, String merchantUid,
                                    String customerUid, String cardNumber, String cardType) {
        return createPayment(order, PaymentType.CARD, amount, impUid, merchantUid, customerUid, cardNumber, cardType);
    }

    /**
     * 정기결제를 처리합니다.
     */
    public Payment processRecurringPayment(Order order, Long amount, String impUid, String merchantUid,
                                         String customerUid, String cardNumber, String cardType,
                                         LocalDateTime nextBillingDate) {
        Payment payment = createPayment(order, PaymentType.CARD, amount, impUid, merchantUid, 
                                      customerUid, cardNumber, cardType);
        payment.setNextBillingDate(nextBillingDate);
        return paymentRepository.save(payment);
    }

    /**
     * 결제가 이미 처리되었는지 확인합니다.
     */
    public boolean isPaymentProcessed(String impUid) {
        return paymentRepository.existsByImpUid(impUid);
    }

    /**
     * 결제 성공 알림 전송
     */
    private void sendPaymentSuccessNotification(Payment payment) {
        try {
            Member member = memberRepository.findById(payment.getOrder().getBuyer().getId())
                    .orElse(null);
            
            if (member != null) {
                String paymentMethod = getPaymentMethodDisplayName(payment.getPaymentType());
                String message = String.format("결제가 성공적으로 완료되었습니다. 금액: %,d%s, 결제수단: %s", 
                    payment.getAmount(), payment.getPaymentType() == PaymentType.POINT ? "P" : "원", paymentMethod);
                
                PaymentNotificationRequest notificationDto = new PaymentNotificationRequest();
                notificationDto.setTargetUserId(member.getId());
                notificationDto.setMessage(message);
                notificationDto.setType("payment_success");
                notificationDto.setCategory(NotificationCategory.ADMIN);
                notificationDto.setLink("/api/orders/payment/success?orderId=" + payment.getOrder().getId());
                notificationDto.setAmount(payment.getAmount());
                notificationDto.setPaymentMethod(paymentMethod);
                notificationDto.setOrderId(payment.getOrder().getId().toString());
                
                // 실시간 알림 전송
                notificationService.sendPaymentNotification(notificationDto);
                
                // DB에 알림 저장
                notificationService.savePaymentNotification(notificationDto, 
                    "/api/orders/payment/success?orderId=" + payment.getOrder().getId());
                
                log.info("결제 성공 알림 전송 완료: userId={}, amount={}", member.getId(), payment.getAmount());
            }
        } catch (Exception e) {
            log.error("결제 성공 알림 전송 실패: paymentId={}, error={}", payment.getId(), e.getMessage());
        }
    }

    /**
     * 결제 실패 알림 전송
     */
    private void sendPaymentFailureNotification(Payment payment, String failureReason) {
        try {
            Member member = memberRepository.findById(payment.getOrder().getBuyer().getId())
                    .orElse(null);
            
            if (member != null) {
                String paymentMethod = getPaymentMethodDisplayName(payment.getPaymentType());
                String message = String.format("결제가 실패했습니다. 금액: %,d%s, 결제수단: %s, 사유: %s", 
                    payment.getAmount(), payment.getPaymentType() == PaymentType.POINT ? "P" : "원", paymentMethod, failureReason);
                
                PaymentNotificationRequest notificationDto = new PaymentNotificationRequest();
                notificationDto.setTargetUserId(member.getId());
                notificationDto.setMessage(message);
                notificationDto.setType("payment_failed");
                notificationDto.setCategory(NotificationCategory.ADMIN);
                notificationDto.setLink("/api/orders/payment/fail?message=" + failureReason);
                notificationDto.setAmount(payment.getAmount());
                notificationDto.setPaymentMethod(paymentMethod);
                notificationDto.setOrderId(payment.getOrder().getId().toString());
                
                // 실시간 알림 전송
                notificationService.sendPaymentNotification(notificationDto);
                
                // DB에 알림 저장
                notificationService.savePaymentNotification(notificationDto, 
                    "/api/orders/payment/fail?message=" + failureReason);
                
                log.info("결제 실패 알림 전송 완료: userId={}, amount={}, reason={}", 
                    member.getId(), payment.getAmount(), failureReason);
            }
        } catch (Exception e) {
            log.error("결제 실패 알림 전송 실패: paymentId={}, error={}", payment.getId(), e.getMessage());
        }
    }

    /**
     * 결제 취소 알림 전송
     */
    private void sendPaymentCancellationNotification(Payment payment) {
        try {
            Member member = memberRepository.findById(payment.getOrder().getBuyer().getId())
                    .orElse(null);
            
            if (member != null) {
                String paymentMethod = getPaymentMethodDisplayName(payment.getPaymentType());
                String message = String.format("결제가 취소되었습니다. 금액: %,d%s, 결제수단: %s", 
                    payment.getAmount(), payment.getPaymentType() == PaymentType.POINT ? "P" : "원", paymentMethod);
                
                PaymentNotificationRequest notificationDto = new PaymentNotificationRequest();
                notificationDto.setTargetUserId(member.getId());
                notificationDto.setMessage(message);
                notificationDto.setType("payment_cancelled");
                notificationDto.setCategory(NotificationCategory.ORDER);
                notificationDto.setLink("/api/orders/payment/cancel?orderId=" + payment.getOrder().getId());
                notificationDto.setAmount(payment.getAmount());
                notificationDto.setPaymentMethod(paymentMethod);
                notificationDto.setOrderId(payment.getOrder().getId().toString());
                
                // 실시간 알림 전송
                notificationService.sendPaymentNotification(notificationDto);
                
                // DB에 알림 저장
                notificationService.savePaymentNotification(notificationDto, 
                    "/api/orders/payment/cancel?orderId=" + payment.getOrder().getId());
                
                log.info("결제 취소 알림 전송 완료: userId={}, amount={}", member.getId(), payment.getAmount());
            }
        } catch (Exception e) {
            log.error("결제 취소 알림 전송 실패: paymentId={}, error={}", payment.getId(), e.getMessage());
        }
    }

    /**
     * 결제 수단 표시명 반환
     */
    private String getPaymentMethodDisplayName(PaymentType paymentType) {
        switch (paymentType) {
            case CARD:
                return "신용카드";
            case POINT:
                return "포인트";
            case BANK_TRANSFER:
                return "계좌이체";
            default:
                return "기타";
        }
    }
} 