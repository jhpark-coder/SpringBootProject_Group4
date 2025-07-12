package com.creatorworks.nexus.auction.dto;

import com.creatorworks.nexus.member.entity.Member;

// Record 타입은 불변(immutable) 데이터 객체를 간결하게 생성하는 최신 Java 기능입니다.
// 생성자, getter, equals, hashCode, toString 메소드가 자동으로 생성됩니다.
public record AuctionDetailViewDto(
        // 1. 기존 AuctionDto의 모든 정보를 포함
        AuctionDto auction,

        // 2. Controller에서 가공하여 채워줄 추가 정보
        String contentHtml,
        boolean hasPaywall,
        boolean canViewContent,
        boolean isFollowing,
        Member currentMember,

        // 3. React 앱에 전달할 데이터 (별도 DTO로 분리)
        ReactBiddingDataDto biddingData
) {
}