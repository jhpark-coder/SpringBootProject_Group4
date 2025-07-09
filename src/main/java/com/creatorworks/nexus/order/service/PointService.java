package com.creatorworks.nexus.order.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.notification.dto.PaymentNotificationRequest;
import com.creatorworks.nexus.notification.entity.NotificationCategory;
import com.creatorworks.nexus.notification.service.NotificationService;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Order.OrderStatus;
import com.creatorworks.nexus.order.entity.Order.OrderType;
import com.creatorworks.nexus.order.entity.Payment;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.repository.PaymentRepository;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    /**
     * 포인트 충전을 처리합니다.
     */
    public Order chargePoint(Long memberId, Long amount, String impUid, String merchantUid) {
        System.out.println("[LOG] chargePoint called: memberId=" + memberId + ", amount=" + amount + ", impUid=" + impUid + ", merchantUid=" + merchantUid); // TODO: 테스트 후 삭제
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));
            System.out.println("[LOG] member found: " + member.getEmail()); // TODO: 테스트 후 삭제

            // 중복 결제 방지
            if (impUid != null && paymentRepository.existsByImpUid(impUid)) {
                System.out.println("[LOG] 이미 처리된 결제: " + impUid); // TODO: 테스트 후 삭제
                throw new IllegalArgumentException("이미 처리된 결제입니다: " + impUid);
            }

            // 주문 생성 (포인트 충전은 상품과 무관하므로 product는 null)
            Order order = Order.builder()
                    .buyer(member)
                    .orderType(OrderType.POINT_PURCHASE)
                    .orderStatus(OrderStatus.PENDING)
                    .totalAmount(amount)
                    .description("포인트 충전: " + amount + "원")
                    .product(null) // 포인트 충전은 상품과 무관
                    .build();

            order = orderRepository.save(order);
            System.out.println("[LOG] order saved: orderId=" + order.getId()); // TODO: 테스트 후 삭제

            // 결제 정보 생성
            Payment payment = paymentService.processPointPayment(order, amount, merchantUid);
            order.setPayment(payment);
            System.out.println("[LOG] payment processed"); // TODO: 테스트 후 삭제

            return orderRepository.save(order);
        } catch (Exception e) {
            System.out.println("[ERROR] chargePoint 예외 발생: " + e.getMessage()); // TODO: 테스트 후 삭제
            e.printStackTrace(); // TODO: 테스트 후 삭제
            throw e;
        }
    }

    /**
     * 포인트로 상품을 구매합니다.
     */
    public Order purchaseWithPoint(Long memberId, Long productId, Integer quantity) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        Long totalAmount = product.getPrice() * quantity;

        // 포인트 잔액 확인
        Long currentBalance = getCurrentBalance(memberId);
        if (currentBalance < totalAmount) {
            throw new IllegalArgumentException("포인트가 부족합니다. 현재 잔액: " + currentBalance + "원, 필요 금액: " + totalAmount + "원");
        }

        // 주문 생성 (상품 구매이므로 product 설정)
        Order order = Order.builder()
                .buyer(member)
                .orderType(OrderType.PRODUCT_PURCHASE)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .description("포인트로 상품 구매: " + product.getName())
                .product(product) // 상품 구매이므로 product 설정
                .build();

        order = orderRepository.save(order);

        // 결제 정보 생성 (포인트 결제)
        String merchantUid = "point_" + System.currentTimeMillis();
        Payment payment = paymentService.processPointPayment(order, totalAmount, merchantUid);
        order.setPayment(payment);

        // 포인트 차감
        member.setPoint((int) (currentBalance - totalAmount));
        memberRepository.save(member);

        // 주문 완료 처리
        order.complete();
        payment.complete();

        return orderRepository.save(order);
    }

    /**
     * 현재 포인트 잔액을 조회합니다.
     */
    public Long getCurrentBalance(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));
        
        return member.getPoint() != null ? member.getPoint().longValue() : 0L;
    }

    /**
     * 포인트 사용 내역을 조회합니다.
     */
    public Page<Order> getPointHistory(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        return orderRepository.findByBuyerAndOrderTypeOrderByOrderDateDesc(member, OrderType.POINT_PURCHASE, pageable);
    }

    /**
     * 포인트를 추가합니다 (관리자용).
     */
    public Order addPoints(Long memberId, Long amount, String description) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        Long currentBalance = getCurrentBalance(memberId);
        Long newBalance = currentBalance + amount;

        // 주문 생성 (관리자 지급, 포인트 충전은 상품과 무관)
        Order order = Order.builder()
                .buyer(member)
                .orderType(OrderType.POINT_PURCHASE)
                .orderStatus(OrderStatus.COMPLETED) // 관리자 지급은 바로 완료
                .totalAmount(amount)
                .description("관리자 지급: " + description)
                .product(null) // 포인트 충전은 상품과 무관
                .build();

        order = orderRepository.save(order);

        // 결제 정보 생성 (관리자 지급)
        String merchantUid = "admin_" + System.currentTimeMillis();
        Payment payment = paymentService.processPointPayment(order, amount, merchantUid);
        payment.complete(); // 관리자 지급은 바로 완료
        order.setPayment(payment);

        // 포인트 추가
        member.setPoint(newBalance.intValue());
        memberRepository.save(member);

        return orderRepository.save(order);
    }

    /**
     * 포인트 충전 완료 처리 (적립 포인트 지정)
     */
    public void completePointCharge(String impUid, Long point) {
        Optional<Payment> paymentOpt = paymentService.findByImpUid(impUid);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            Order order = payment.getOrder();
            if (order.getOrderType() == OrderType.POINT_PURCHASE) {
                // Order에 product가 설정되어 있지 않으면 설정 (포인트 충전은 상품과 무관)
                if (order.getProduct() == null) {
                    log.info("포인트 충전 Order 완료 처리: orderId={}", order.getId());
                }
                
                // 포인트 추가 (지정된 point 사용)
                Member member = order.getBuyer();
                Long currentBalance = getCurrentBalance(member.getId());
                Long newBalance = currentBalance + point;
                member.setPoint(newBalance.intValue());
                memberRepository.save(member);
                
                // 결제 완료 처리
                paymentService.completePayment(impUid);
                
                // 포인트 충전 성공 알림 전송
                sendPointChargeSuccessNotification(member, point, newBalance);
                
                log.info("포인트 충전 완료(지정): memberId={}, point={}, newBalance={}", member.getId(), point, newBalance);
            }
        }
    }

    /**
     * 포인트 충전 성공 알림 전송
     */
    private void sendPointChargeSuccessNotification(Member member, Long chargedAmount, Long newBalance) {
        try {
            String message = String.format("포인트가 성공적으로 충전되었습니다. 충전 금액: %,d원, 현재 잔액: %,d원", 
                chargedAmount, newBalance);
            
            PaymentNotificationRequest notificationDto = new PaymentNotificationRequest();
            notificationDto.setTargetUserId(member.getId());
            notificationDto.setMessage(message);
            notificationDto.setType("payment_success");
            notificationDto.setCategory(NotificationCategory.ADMIN);
            notificationDto.setLink("/member/myPage/" + member.getId() + "/points");
            notificationDto.setAmount(chargedAmount);
            notificationDto.setPaymentMethod("포인트 충전");
            notificationDto.setOrderId("point_" + System.currentTimeMillis());
            
            // 실시간 알림 전송
            notificationService.sendPaymentNotification(notificationDto);
            
            // DB에 알림 저장
            notificationService.savePaymentNotification(notificationDto, "/member/myPage/" + member.getId() + "/points");
            
            log.info("포인트 충전 성공 알림 전송 완료: userId={}, chargedAmount={}, newBalance={}", 
                member.getId(), chargedAmount, newBalance);
        } catch (Exception e) {
            log.error("포인트 충전 성공 알림 전송 실패: userId={}, error={}", member.getId(), e.getMessage());
        }
    }

    private void sendPointChargeFailureNotification(Member member, Long failedAmount, String reason) {
        try {
            String message = String.format("포인트 충전에 실패했습니다. 시도 금액: %,d원, 사유: %s", failedAmount, reason);
            
            PaymentNotificationRequest notificationDto = new PaymentNotificationRequest();
            notificationDto.setTargetUserId(member.getId());
            notificationDto.setMessage(message);
            notificationDto.setType("payment_failed");
            notificationDto.setCategory(NotificationCategory.ADMIN);
            notificationDto.setLink("/api/orders/points/charge-page"); // 실패시 charge-page로 이동
            notificationDto.setAmount(failedAmount);
            notificationDto.setPaymentMethod("포인트 충전");
            notificationDto.setOrderId("point_" + System.currentTimeMillis());
            
            // 실시간 알림 전송
            notificationService.sendPaymentNotification(notificationDto);
            
            // DB에 알림 저장
            notificationService.savePaymentNotification(notificationDto, "/api/orders/points/charge-page");
            
            log.info("포인트 충전 실패 알림 전송 완료: userId={}, failedAmount={}, reason={}", 
                member.getId(), failedAmount, reason);
        } catch (Exception e) {
            log.error("포인트 충전 실패 알림 전송 실패: userId={}, error={}", member.getId(), e.getMessage());
        }
    }

    public static class PointHistoryDto {
        private String description;
        private Long amount;
        private String type; // CHARGE/USE 등
        private java.time.LocalDateTime regTime;
        private Long balanceAfter;
        
        // Getter methods
        public String getDescription() { return description; }
        public Long getAmount() { return amount; }
        public String getType() { return type; }
        public java.time.LocalDateTime getRegTime() { return regTime; }
        public Long getBalanceAfter() { return balanceAfter; }
        
        // Setter methods
        public void setDescription(String description) { this.description = description; }
        public void setAmount(Long amount) { this.amount = amount; }
        public void setType(String type) { this.type = type; }
        public void setRegTime(java.time.LocalDateTime regTime) { this.regTime = regTime; }
        public void setBalanceAfter(Long balanceAfter) { this.balanceAfter = balanceAfter; }
    }

    public Page<PointHistoryDto> getPointHistoryDtoList(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        Page<Order> orderPage = orderRepository.findByBuyerOrderByOrderDateDesc(member, pageable);

        return orderPage.map(order -> {
            PointHistoryDto dto = new PointHistoryDto();
            dto.setRegTime(order.getOrderDate());
            dto.setDescription(order.getDescription());

            if (order.getOrderType() == OrderType.POINT_PURCHASE) {
                dto.setType("CHARGE");
                dto.setAmount(order.getTotalAmount());
            } else {
                dto.setType("USE");
                dto.setAmount(order.getTotalAmount());
            }

            dto.setBalanceAfter(null); // 각 거래 후 잔액 계산은 복잡하므로 여기서는 null로 처리
            return dto;
        });
    }
} 