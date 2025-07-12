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
        String sellerEmail, // [추가!] 판매자 이메일 필드
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
                auction.getImageUrl(),
                auction.getPrimaryCategory(),
                auction.getSecondaryCategory(),
                auction.getDescription(),
                auction.getTiptapJson(),
                auction.getWorkDescription(),
                auction.getBackgroundColor(),
                auction.getFontFamily(),
                (auction.getSeller() != null) ? auction.getSeller().getName() : "Unknown",
                (auction.getSeller() != null) ? auction.getSeller().getId() : null,
                (auction.getSeller() != null) ? auction.getSeller().getEmail() : null,
                auction.getAuctionDuration(),
                auction.getStartBidPrice(),
                auction.getBuyNowPrice(),
                auction.getRegTime(),
                auction.getUpdateTime(),
                auction.getAuctionEndTime(),
                auction.getItemTags().stream()
                        .map(productItemTag -> productItemTag.getItemTag().getName())
                        .collect(Collectors.toList())
        );
    }
}