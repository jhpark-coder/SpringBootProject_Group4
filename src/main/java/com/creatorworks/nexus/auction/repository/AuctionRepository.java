package com.creatorworks.nexus.auction.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.auction.entity.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long>, JpaSpecificationExecutor<Auction> {
    Page<Auction> findAll(Pageable pageable);
    
    // 종료된 경매 조회
    @Query("SELECT a FROM Auction a WHERE a.auctionEndTime < :now")
    List<Auction> findByAuctionEndTimeBefore(@Param("now") LocalDateTime now);

    // 판매자별 경매 목록 조회
    Page<Auction> findBySeller(com.creatorworks.nexus.member.entity.Member seller, Pageable pageable);

    // 판매자별 + 진행중인 경매 목록 조회
    List<Auction> findBySellerAndAuctionEndTimeAfter(com.creatorworks.nexus.member.entity.Member seller, java.time.LocalDateTime now);
}
