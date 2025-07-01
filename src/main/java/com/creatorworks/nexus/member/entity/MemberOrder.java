package com.creatorworks.nexus.member.entity;

import com.creatorworks.nexus.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//20250630 차트 추가를 위해 작성
// 경로: com.creatorworks.nexus.order.entity.MemberOrder.java
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 누가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 무엇을

    @Column(nullable = false)
    private LocalDateTime orderDate; // 언제

    //0630 여기까지 ... (builder 등)
}