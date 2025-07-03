package com.creatorworks.nexus.product.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(nullable = false)
    private Long amount; // 포인트 금액 (양수: 충전, 음수: 사용)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType type; // 포인트 타입 (CHARGE: 충전, USE: 사용)
    
    @Column(nullable = false)
    private Long balanceAfter; // 거래 후 잔액
    
    @Column(length = 100)
    private String description; // 거래 설명
    
    @Column(length = 50)
    private String impUid; // 아임포트 결제 UID (충전 시)
    
    @Column(length = 50)
    private String merchantUid; // 주문 UID
    
    @Builder
    public Point(Member member, Product product, Long amount, PointType type, 
                 Long balanceAfter, String description, String impUid, String merchantUid) {
        this.member = member;
        this.product = product;
        this.amount = amount;
        this.type = type;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.impUid = impUid;
        this.merchantUid = merchantUid;
    }
    
    public enum PointType {
        CHARGE("충전"),
        USE("사용");
        
        private final String description;
        
        PointType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}