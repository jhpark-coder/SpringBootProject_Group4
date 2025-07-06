package com.creatorworks.nexus.product.dto;

// import com.creatorworks.nexus.product.entity.ItemTag; // 사용하지 않으므로 삭제
import java.util.List;
import java.util.stream.Collectors;

import com.creatorworks.nexus.product.entity.Product;

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
        List<String> tags
) {
    public ProductDto(Product product) {
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
                        .collect(Collectors.toList())
        );
    }
} 