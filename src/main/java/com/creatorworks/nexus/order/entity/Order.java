package com.creatorworks.nexus.order.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.Product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Builder
    public Order(Member buyer, Product product, LocalDateTime orderDate) {
        this.buyer = buyer;
        this.product = product;
        this.orderDate = (orderDate != null) ? orderDate : LocalDateTime.now();
    }
}
