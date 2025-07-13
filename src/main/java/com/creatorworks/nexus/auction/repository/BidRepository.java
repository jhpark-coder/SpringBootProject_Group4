package com.creatorworks.nexus.auction.repository;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.Bid;
import com.creatorworks.nexus.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// JpaRepository를 상속받으면 기본적인 DB 작업 메소드(save, findById 등)를 자동으로 쓸 수 있어요.
public interface BidRepository extends JpaRepository<Bid, Long> {
    // 앞으로 필요하다면 여기에 특정 조건으로 Bid를 찾는 메소드를 추가할 수 있어요.
    // 예를 들어, 특정 경매의 모든 입찰 기록을 찾는다거나 하는...

    /**
     * 특정 사용자가 입찰한 모든 경매 목록을 중복 없이, 최신 입찰 순으로 조회합니다.
     * @param bidder 입찰자(Member) 객체
     * @param pageable 페이지 정보
     * @return 해당 사용자가 입찰한 경매의 Page 객체
     */
    // JPQL(Java Persistence Query Language)을 사용한 커스텀 쿼리
    @Query("SELECT b.auction FROM Bid b WHERE b.bidder = :bidder " +
            "AND b.regTime = (SELECT MAX(b2.regTime) FROM Bid b2 WHERE b2.auction = b.auction AND b2.bidder = :bidder) " +
            "ORDER BY b.regTime DESC")
    Page<Auction> findAuctionsByBidder(@Param("bidder") Member bidder, Pageable pageable);
}