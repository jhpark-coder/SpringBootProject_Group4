package com.creatorworks.nexus.auction.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuctionSaveRequest {
    private String name;
    private String imageUrl;
    private String primaryCategory;
    private String secondaryCategory;
    private String description;
    private String tiptapJson;
    private String htmlBackup;
    private String backgroundColor;
    private String fontFamily;
    private int auctionDuration;
    private int startBidPrice;
    private int buyNowPrice;
} 