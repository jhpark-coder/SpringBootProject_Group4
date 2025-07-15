package com.creatorworks.nexus.auction.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.auction.entity.Bid;
import com.creatorworks.nexus.member.entity.Member;

public interface BidRepository extends JpaRepository<Bid, Long> {

    // 특정 경매의 모든 입찰 기록을 시간순으로 조회
    List<Bid> findByAuctionIdOrderByRegTimeDesc(Long auctionId);

    // 특정 사용자가 입찰한 경매 목록 조회 (페이징) - 최신 입찰 기준으로 중복 제거
    @Query("SELECT b.auction FROM Bid b WHERE b.bidder = :bidder " +
            "AND b.regTime = (SELECT MAX(b2.regTime) FROM Bid b2 WHERE b2.auction = b.auction AND b2.bidder = :bidder) " +
            "ORDER BY b.regTime DESC")
    Page<com.creatorworks.nexus.auction.entity.Auction> findAuctionsByBidder(@Param("bidder") Member bidder, Pageable pageable);

    // 특정 경매의 최고 입찰 기록 조회
    @Query("SELECT b FROM Bid b WHERE b.auction.id = :auctionId ORDER BY b.price DESC, b.regTime ASC")
    List<Bid> findTopBidsByAuctionId(@Param("auctionId") Long auctionId, Pageable pageable);

    // 특정 경매의 최고 입찰가 조회
    @Query("SELECT MAX(b.price) FROM Bid b WHERE b.auction.id = :auctionId")
    Long findMaxPriceByAuctionId(@Param("auctionId") Long auctionId);

    // 특정 사용자가 특정 경매에 입찰했는지 확인
    boolean existsByAuctionIdAndBidderId(Long auctionId, Long bidderId);
} 