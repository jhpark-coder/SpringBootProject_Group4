package com.creatorworks.nexus.product.dto;

import com.creatorworks.nexus.product.entity.ItemTag;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductItemTag;

import java.util.List;
import java.util.stream.Collectors;

public record ProductDto(
        Long id,
        String name,
        Long price,
        String description,
        String workDescription,
        String tiptapJson,
        String imageUrl,
        String primaryCategory,
        String secondaryCategory,
        String backgroundColor,
        String fontFamily,
        String sellerName,
        Long sellerId,
        java.time.LocalDateTime regTime,
        java.time.LocalDateTime updateTime,
        List<String> tags,
        boolean isFollowing // 내가 팔로우 중인지
) {
    public ProductDto(Product product, boolean isFollowing) {
        this(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getWorkDescription(),
                product.getTiptapJson(),
                product.getImageUrl(),
                product.getPrimaryCategory(),
                product.getSecondaryCategory(),
                product.getBackgroundColor(),
                product.getFontFamily(),
                (product.getSeller() != null) ? product.getSeller().getName() : "Unknown",
                (product.getSeller() != null) ? product.getSeller().getId() : null,
                product.getRegTime(),
                product.getUpdateTime(),
                product.getItemTags().stream()
                        .map(productItemTag -> productItemTag.getItemTag().getName())
                        .collect(Collectors.toList()),
                isFollowing
        );
    }
    // 기존 ProductDto(Product product) 생성자도 유지
    public ProductDto(Product product) {
        this(product, false);
    }
} 