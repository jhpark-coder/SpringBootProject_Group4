package com.creatorworks.nexus.product.entity;

import java.util.ArrayList;
import java.util.List;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductInquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isSecret = false; // 비밀글 여부, 기본값은 false

    // 부모-자식 관계 (대댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductInquiry parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<ProductInquiry> children = new ArrayList<>();

    @Builder
    public ProductInquiry(Product product, Member writer, String content, boolean isSecret, ProductInquiry parent) {
        this.product = product;
        this.writer = writer;
        this.content = content;
        this.isSecret = isSecret;
        this.parent = parent;
    }

    //== 연관관계 편의 메서드 ==//
    public void setParent(ProductInquiry parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }
} 