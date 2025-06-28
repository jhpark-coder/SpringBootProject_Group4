package com.creatorworks.nexus.product.entity;

import java.io.Serializable;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    private String name;
    private int price;
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

    @Builder
    public Product(Member author, String name, int price, String description, String workDescription, String tiptapJson, String imageUrl, String primaryCategory, String secondaryCategory, String backgroundColor, String fontFamily) {
        this.author = author;
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
    }
}
