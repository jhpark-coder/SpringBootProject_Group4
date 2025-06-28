package com.creatorworks.nexus.product.dto;

import com.creatorworks.nexus.product.entity.Product;

import lombok.Getter;

@Getter
public class ProductDto {
    private final Long id;
    private final String name;
    private final int price;
    private final String imageUrl;
    private final String authorName;

    public ProductDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();
        this.authorName = (product.getAuthor() != null) ? product.getAuthor().getName() : "Unknown";
    }
} 