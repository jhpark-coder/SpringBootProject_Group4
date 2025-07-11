package com.creatorworks.nexus.auction.repository;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.AuctionInquiry;
import com.creatorworks.nexus.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuctionInquiryRepository extends JpaRepository<AuctionInquiry, Long> {
    /**
     * 특정 상품에 달린 모든 문의와 답변을 작성일 순으로 조회합니다.
     * 부모가 없는 문의(최상위 글)가 먼저 오고, 그 다음에 각 문의에 대한 답변들이 정렬됩니다.
     * @param auctionId 상품 ID
     * @return 정렬된 문의 목록
     */
    List<AuctionInquiry> findByAuction_IdOrderByParent_IdAscRegTimeAsc(Long auctionId);

    Page<AuctionInquiry> findByAuctionAndParentIsNull(Auction auction, Pageable pageable);

    Page<AuctionInquiry> findByWriter(Member writer, Pageable pageable);

    /**
     * 판매자가 등록한 모든 상품에 대한 (최상위) 문의 목록을 조회합니다.
     * 답변을 제외하고 질문 글(parent null)만 가져옵니다.
     */
    @Query("SELECT i FROM AuctionInquiry i WHERE i.auction.seller = :seller AND i.parent IS NULL")
    Page<AuctionInquiry> findBySeller(@Param("seller") Member seller, Pageable pageable);
}
