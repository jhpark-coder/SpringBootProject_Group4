package com.creatorworks.nexus.order.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.dto.TopSellingProductDto;
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
public class OrderService {

    private final OrderRepository orderRepository;

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;
    private final PointService pointService;

    // === 통합 주문 관리 ===

    /**
     * 주문을 생성합니다.
     */
    public Order createOrder(Member buyer, OrderType orderType, Long totalAmount, String description, Product product) {
        Order order = Order.builder()
                .buyer(buyer)
                .orderType(orderType)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .description(description)
                .product(product)
                .build();
        return orderRepository.save(order);
    }



    /**
     * 주문을 완료 처리합니다.
     */
    public Order completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        order.complete();
        if (order.getPayment() != null) {
            order.getPayment().complete();
        }

        return orderRepository.save(order);
    }

    /**
     * 주문을 취소 처리합니다.
     */
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        order.cancel();
        if (order.getPayment() != null) {
            order.getPayment().cancel();
        }

        return orderRepository.save(order);
    }

    /**
     * 구매자의 주문 목록을 조회합니다.
     */
    public Page<Order> getOrdersByBuyer(Member buyer, Pageable pageable) {
        return orderRepository.findByBuyerOrderByOrderDateDesc(buyer, pageable);
    }

    /**
     * 구매자의 특정 타입 주문 목록을 조회합니다.
     */
    public Page<Order> getOrdersByBuyerAndType(Member buyer, OrderType orderType, Pageable pageable) {
        return orderRepository.findByBuyerAndOrderTypeOrderByOrderDateDesc(buyer, orderType, pageable);
    }

    /**
     * 주문 ID로 주문을 조회합니다.
     */
    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * impUid로 주문을 조회합니다.
     */
    public Optional<Order> findByImpUid(String impUid) {
        return orderRepository.findByPayment_ImpUid(impUid);
    }

    /**
     * merchantUid로 주문을 조회합니다.
     */
    public Optional<Order> findByMerchantUid(String merchantUid) {
        return orderRepository.findByPayment_MerchantUid(merchantUid);
    }

    // === 상품 구매 관련 ===

    /**
     * 상품을 구매합니다.
     */
    public Order purchaseProduct(Long buyerId, Long productId, Integer quantity, 
                               String impUid, String merchantUid, String customerUid,
                               String cardNumber, String cardType) {
        Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다: " + buyerId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        Long totalAmount = product.getPrice() * quantity;

        // 주문 생성 (상품 구매이므로 product 설정)
        Order order = Order.builder()
                .buyer(buyer)
                .orderType(OrderType.PRODUCT_PURCHASE)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .description("상품 구매: " + product.getName())
                .product(product) // 상품 구매이므로 product 설정
                .build();

        order = orderRepository.save(order);

        // 결제 정보 생성
        Payment payment = paymentService.processCardPayment(order, totalAmount, impUid, 
                                                          merchantUid, customerUid, cardNumber, cardType);
        order.setPayment(payment);

        return orderRepository.save(order);
    }

    /**
     * 포인트로 상품을 구매합니다.
     */
    public Order purchaseProductWithPoint(Long buyerId, Long productId, Integer quantity) {
        return pointService.purchaseWithPoint(buyerId, productId, quantity);
    }

    // === 구독 관련 ===

    /**
     * 구독을 생성합니다.
     */
    public Order createSubscription(Long subscriberId, Long authorId, Integer months, Long amount,
                                  String impUid, String merchantUid, String customerUid,
                                  String cardNumber, String cardType) {
        Member subscriber = memberRepository.findById(subscriberId)
                .orElseThrow(() -> new IllegalArgumentException("구독자를 찾을 수 없습니다: " + subscriberId));

        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다: " + authorId));

        Long totalAmount = amount * months;

        // 주문 생성 (구독이므로 product는 null, author 정보는 description에 포함)
        Order order = Order.builder()
                .buyer(subscriber)
                .orderType(OrderType.SUBSCRIPTION)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .description("구독: " + author.getName() + " (" + months + "개월)")
                .product(null) // 구독은 상품과 무관
                .build();

        order = orderRepository.save(order);

        // 정기결제 정보 생성
        LocalDateTime nextBillingDate = LocalDateTime.now().plusMonths(1);
        Payment payment = paymentService.processRecurringPayment(order, totalAmount, impUid,
                                                               merchantUid, customerUid, cardNumber, cardType,
                                                               nextBillingDate);
        order.setPayment(payment);

        return orderRepository.save(order);
    }

    /**
     * 다음 결제일이 임박한 구독을 조회합니다.
     */
    public List<Order> findUpcomingSubscriptionOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);
        return orderRepository.findUpcomingSubscriptionOrders(now, threeDaysLater);
    }

    // === 기존 통계 관련 메서드들 (호환성 유지) ===

    /**
     * 특정 사용자가 특정 상품을 구매했는지 여부를 확인합니다.
     */
    public boolean hasPurchasedProduct(Member buyer, Product product) {
        return orderRepository.existsByBuyerAndProduct(buyer, product);
    }

    /**
     * 특정 상품의 구매 횟수를 조회합니다.
     */
    public long getProductPurchaseCount(Long productId) {
        return orderRepository.countByProductId(productId);
    }

    /**
     * 특정 사용자의 전체 주문 건수를 조회합니다.
     */
    public long getOrderCountByBuyer(Member buyer) {
        return orderRepository.countByBuyer(buyer);
    }

    /**
     * 특정 기간 동안 특정 판매자의 상품 중 가장 많이 팔린 상품 목록을 조회합니다.
     */
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