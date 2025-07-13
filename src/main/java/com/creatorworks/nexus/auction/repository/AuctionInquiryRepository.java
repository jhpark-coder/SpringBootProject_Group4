package com.creatorworks.nexus.auction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.AuctionInquiry;

@Repository
public interface AuctionInquiryRepository extends JpaRepository<AuctionInquiry, Long> {
    
    /**
     * 특정 경매의 최상위 문의들만 조회합니다 (답변 제외)
     * @param auction 경매 엔티티
     * @param pageable 페이징 정보
     * @return 페이징된 문의 목록
     */
    Page<AuctionInquiry> findByAuctionAndParentIsNull(Auction auction, Pageable pageable);
} 