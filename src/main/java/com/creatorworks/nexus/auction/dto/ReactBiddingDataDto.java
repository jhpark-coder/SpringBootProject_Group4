package com.creatorworks.nexus.auction.dto;

import java.time.LocalDateTime;

public record ReactBiddingDataDto(
        Long auctionId,
        Long initialHighestBid,
        Long buyNowPrice,
        LocalDateTime auctionEndTime,
        String authToken
) {
}