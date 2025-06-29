package com.creatorworks.nexus.auction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.creatorworks.nexus.auction.entity.AuctionItemTag;

public interface AuctionItemTagRepository extends JpaRepository<AuctionItemTag, Long> {
    List<AuctionItemTag> findAllByAuctionId(Long auctionId);

    @Modifying
    @Query("delete from AuctionItemTag ait where ait.auction.id = :auctionId")
    void deleteAllByAuctionId(Long auctionId);
} 