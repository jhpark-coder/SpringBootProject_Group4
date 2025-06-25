package com.creatorworks.nexus.product.entity;

import java.io.Serializable;

import com.creatorworks.nexus.global.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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

    public Product(String name, int price, String description, String imageUrl) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
