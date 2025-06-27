package com.creatorworks.nexus.product.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.product.constant.ProductCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    private String description;
    private String imageUrl;

    private String productNumber;
    private String productTitle;
//    private String category2;
    private String heart;
    private String orderId;
    private String regId;

    @Enumerated(EnumType.STRING)
    private ProductCategory category1;

    public Product(String name, String description, String imageUrl, ProductCategory category1) {
        this.name = name;
//        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
//        this.productNumber = productNumber;
//        this.productTitle = productTitle;
        this.category1 = category1;
//        this.category2 = category2;
//        this.heart = heart;
//        this.orderId = orderId;
//        this.regId = regId;
    }
}
