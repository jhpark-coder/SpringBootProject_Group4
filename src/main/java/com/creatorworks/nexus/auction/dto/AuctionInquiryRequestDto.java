package com.creatorworks.nexus.auction.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuctionInquiryRequestDto {
    private String content;

    private Boolean isSecret;

}
