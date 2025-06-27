package com.creatorworks.nexus.product.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.ProductHeart;
import com.creatorworks.nexus.member.entity.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Product extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    @Lob
    private String description;
    @Lob
    private String tiptapJson;
    @Lob
    private String imageUrl;
    private String primaryCategory;
    private String secondaryCategory;
    private String backgroundColor;
    private String fontFamily;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductHeart> productHearts = new ArrayList<>();

    // 작가 정보 (Member와의 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Member creator;

    // 작가 이름 (별도 필드로 저장)
    private String creatorName;

    public Product(String name, int price, String description, String imageUrl) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getHeartCount() {
        if (this.productHearts == null) {
            return 0;
        }
        return this.productHearts.size();
    }
}
