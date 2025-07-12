package com.creatorworks.nexus.product.entity;

import java.io.Serializable;
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
import lombok.ToString;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"seller", "inquiries", "itemTags"})
public class Product extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Member seller;

    private String name;
    private Long price;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String workDescription;
    @Lob
    private String tiptapJson;
    @Lob
    private String imageUrl;
    private String primaryCategory;
    private String secondaryCategory;
    private String backgroundColor;
    private String fontFamily;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long viewCount = 0L;

    // 크롤링 관련 필드
    @Column(columnDefinition = "TEXT")
    private String crawledAuthorName;  // 크롤링된 원본 작가명
    
    @Column(columnDefinition = "TEXT")
    private String crawledSourceUrl;   // 크롤링된 원본 URL
    
    @Column(columnDefinition = "TEXT")
    private String crawledTags;        // 크롤링된 원본 태그들

    @OneToMany(mappedBy = "product", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ProductInquiry> inquiries = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ProductItemTag> itemTags = new ArrayList<>();

    @Builder
    public Product(Member seller, String name, Long price, String description, String workDescription, String tiptapJson, String imageUrl, String primaryCategory, String secondaryCategory, String backgroundColor, String fontFamily, String crawledAuthorName, String crawledSourceUrl, String crawledTags) {
        this.seller = seller;
        this.name = name;
        this.price = price;
        this.description = description;
        this.workDescription = workDescription;
        this.tiptapJson = tiptapJson;
        this.imageUrl = imageUrl;
        this.primaryCategory = primaryCategory;
        this.secondaryCategory = secondaryCategory;
        this.backgroundColor = backgroundColor;
        this.fontFamily = fontFamily;
        this.viewCount = 0L;
        this.crawledAuthorName = crawledAuthorName;
        this.crawledSourceUrl = crawledSourceUrl;
        this.crawledTags = crawledTags;
    }

    public void addReview(ProductReview review) {
        // Implementation of addReview method
    }

    public void addInquiry(ProductInquiry inquiry) {
        this.inquiries.add(inquiry);
        inquiry.setProduct(this);
    }
}
