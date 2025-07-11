package com.creatorworks.nexus.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BiddingResponseDto {
    private boolean success;            // 성공 여부
    private String message;             // 처리 결과 메시지
    private Long newHighestBid;         // 새로운 최고 입찰가
    private String highestBidderName;   // 최고 입찰자 이름
}