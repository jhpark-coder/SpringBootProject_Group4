package com.creatorworks.nexus.auction.dto;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.product.entity.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record AuctionDto(
        Long id,
        String name,
        String imageUrl,
        String primaryCategory,
        String secondaryCategory,
        String description,
        String tiptapJson,
        String workDescription,
        String backgroundColor,
        String fontFamily,
        String sellerName,
        Long sellerId,
        int auctionDuration,
        Long startBidPrice,
        Long buyNowPrice,
        java.time.LocalDateTime regTime,
        java.time.LocalDateTime updateTime,
        LocalDateTime auctionEndTime,
        List<String> tags
) {
    public AuctionDto(Auction auction) {
        this(
                auction.getId(),
                auction.getName(),
                auction.getDescription(),
                auction.getWorkDescription(),
                auction.getTiptapJson(),
                auction.getImageUrl(),
                auction.getPrimaryCategory(),
                auction.getSecondaryCategory(),
                auction.getBackgroundColor(),
                auction.getFontFamily(),
                (auction.getSeller() != null) ? auction.getSeller().getName() : "Unknown",
                (auction.getSeller() != null) ? auction.getSeller().getId() : null,
                auction.getAuctionDuration(),
                auction.getBuyNowPrice(),
                auction.getStartBidPrice(),
                auction.getRegTime(),
                auction.getUpdateTime(),
                auction.getAuctionEndTime(),
                auction.getItemTags().stream()
                        .map(productItemTag -> productItemTag.getItemTag().getName())
                        .collect(Collectors.toList())
        );
    }
}