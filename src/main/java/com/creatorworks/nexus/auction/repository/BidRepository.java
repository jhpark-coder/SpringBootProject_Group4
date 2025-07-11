package com.creatorworks.nexus.auction.repository;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    // 특정 경매의 최고 입찰가를 찾는 쿼리
    @Query("SELECT b FROM Bid b WHERE b.auction = :auction ORDER BY b.amount DESC, b.bidTime ASC LIMIT 1")
    Optional<Bid> findTopByAuctionOrderByAmountDesc(@Param("auction") Auction auction);
}