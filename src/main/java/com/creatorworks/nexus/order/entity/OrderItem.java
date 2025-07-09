package com.creatorworks.nexus.order.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.Product;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // null일 수 있음 (구독의 경우)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author; // 구독 대상 작가

    @Column(nullable = false)
    private Long price; // 개별 항목 가격

    @Column(nullable = false)
    private Integer quantity; // 수량

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType; // PRODUCT, SUBSCRIPTION, POINT_CHARGE

    @Column(length = 200)
    private String itemName; // 상품명 또는 구독명

    @Column(length = 500)
    private String description; // 상세 설명

    @Builder
    public OrderItem(Order order, Product product, Member author, Long price, 
                    Integer quantity, ItemType itemType, String itemName, String description) {
        this.order = order;
        this.product = product;
        this.author = author;
        this.price = price;
        this.quantity = quantity;
        this.itemType = itemType;
        this.itemName = itemName;
        this.description = description;
    }

    // 총 가격 계산
    public Long getTotalPrice() {
        return price * quantity;
    }

    public enum ItemType {
        PRODUCT("상품"),
        SUBSCRIPTION("구독"),
        POINT_CHARGE("포인트 충전");

        private final String description;

        ItemType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 