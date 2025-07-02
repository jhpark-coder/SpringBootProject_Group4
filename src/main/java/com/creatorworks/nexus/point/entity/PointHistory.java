package com.creatorworks.nexus.point.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "point_history")
@Getter
@Setter
public class PointHistory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType type; // EARN, USE, REFUND
    
    @Column(nullable = false)
    private Integer amount; // 포인트 금액
    
    @Column(nullable = false)
    private Integer balanceAfter; // 거래 후 잔액
    
    @Column(length = 500)
    private String description; // 거래 설명
    
    @Column(name = "related_id")
    private Long relatedId; // 관련 상품/주문 ID
    
    @Column(name = "related_type")
    private String relatedType; // 관련 타입 (PRODUCT, ORDER 등)
    
    public enum PointType {
        EARN,   // 적립
        USE,    // 사용
        REFUND  // 환불
    }
} 