package com.creatorworks.nexus.auction.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class AuctionPageResponse {

    private final List<AuctionDto> auctions;
    private final int currentPage;
    private final int totalPages;
    private final long totalElements;
    private final int pageSize;
    private final boolean isFirst;
    private final boolean isLast;

    public AuctionPageResponse(List<AuctionDto> auctions, int currentPage, int totalPages, long totalElements, int pageSize, boolean isFirst, boolean isLast) {
        this.auctions = auctions;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.isFirst = isFirst;
        this.isLast = isLast;
    }
}
