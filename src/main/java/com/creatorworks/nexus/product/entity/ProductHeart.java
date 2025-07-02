package com.creatorworks.nexus.product.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="product_heart")
@Entity
public class ProductHeart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

      // JPA를 위한 기본 생성자 (반드시 필요)
    public ProductHeart() {
    }

    // 서비스 로직에서 사용할 생성자
    public ProductHeart(Member member, Product product) {
        this.member = member;
        this.product = product;
    }
} 