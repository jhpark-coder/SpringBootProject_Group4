package com.creatorworks.nexus.order.entity;

import java.time.LocalDateTime;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.Product;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType; // POINT_PURCHASE, SUBSCRIPTION, PRODUCT_PURCHASE

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus; // PENDING, COMPLETED, CANCELLED, FAILED

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount; // 총 결제 금액

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(length = 500)
    private String description; // 주문 설명

    // 단일 주문을 위한 상품 정보 (홈페이지에서는 모든 주문이 단일 주문)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;



    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; // 구매한 상품을 실제로 확인했는지 여부

    @Builder
    public Order(Member buyer, OrderType orderType, OrderStatus orderStatus, 
                Long totalAmount, LocalDateTime orderDate, String description, Product product) {
        this.buyer = buyer;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.orderDate = (orderDate != null) ? orderDate : LocalDateTime.now();
        this.description = description;
        this.product = product;
        this.isRead = false; // 기본값은 false
    }

    // 주문 상태 변경 메서드들
    public void complete() {
        this.orderStatus = OrderStatus.COMPLETED;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void fail() {
        this.orderStatus = OrderStatus.FAILED;
    }



    // Payment 설정 메서드
    public void setPayment(Payment payment) {
        this.payment = payment;
        payment.setOrder(this);
    }

    // isRead 상태 변경 메서드
    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsUnread() {
        this.isRead = false;
    }

    public boolean isRead() {
        return this.isRead;
    }

    public enum OrderType {
        POINT_PURCHASE("포인트 구매"),
        SUBSCRIPTION("구독"),
        PRODUCT_PURCHASE("상품 구매");

        private final String description;

        OrderType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum OrderStatus {
        PENDING("대기중"),
        COMPLETED("완료"),
        CANCELLED("취소"),
        FAILED("실패");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
