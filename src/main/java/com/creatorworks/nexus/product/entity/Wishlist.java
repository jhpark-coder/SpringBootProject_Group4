package com.creatorworks.nexus.product.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "wishlist")
public class Wishlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 찜한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 찜한 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 찜한 날짜
    @Column(nullable = false)
    private LocalDateTime wishDate;

    // 생성자
    public Wishlist(Member member, Product product) {
        this.member = member;
        this.product = product;
        this.wishDate = LocalDateTime.now();
    }

    // 찜하기 전에 이미 찜했는지 확인하는 메서드
    public boolean isWishedBy(Member member) {
        return this.member.getId().equals(member.getId());
    }
} 