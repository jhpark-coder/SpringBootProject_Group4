package com.creatorworks.nexus.auction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.AuctionPayment;
import com.creatorworks.nexus.auction.entity.PaymentStatus;
import com.creatorworks.nexus.member.entity.Member;

@Repository
public interface AuctionPaymentRepository extends JpaRepository<AuctionPayment, Long> {

    // 특정 경매의 결제 내역 조회
    List<AuctionPayment> findByAuctionIdOrderByPaymentDateDesc(Long auctionId);

    // 특정 사용자의 결제 내역 조회 (페이징)
    Page<AuctionPayment> findByBidderOrderByPaymentDateDesc(Member bidder, Pageable pageable);

    // 특정 사용자의 결제 내역 조회 (상태별)
    List<AuctionPayment> findByBidderAndStatusOrderByPaymentDateDesc(Member bidder, PaymentStatus status);

    // 아임포트 UID로 결제 조회
    Optional<AuctionPayment> findByImpUid(String impUid);

    // 주문 UID로 결제 조회
    Optional<AuctionPayment> findByMerchantUid(String merchantUid);

    // 특정 경매의 성공한 결제 조회
    Optional<AuctionPayment> findByAuctionIdAndStatus(Long auctionId, PaymentStatus status);

    // 사용자별 결제 통계
    @Query("SELECT COUNT(ap) FROM AuctionPayment ap WHERE ap.bidder = :bidder AND ap.status = :status")
    long countByBidderAndStatus(@Param("bidder") Member bidder, @Param("status") PaymentStatus status);

    // 사용자별 총 결제 금액
    @Query("SELECT SUM(ap.amount) FROM AuctionPayment ap WHERE ap.bidder = :bidder AND ap.status = 'SUCCESS'")
    Long getTotalPaymentAmountByBidder(@Param("bidder") Member bidder);

    /**
     * 특정 사용자가 특정 경매 상품을 성공적으로 구매했는지 여부를 확인합니다.
     * @param bidder 구매자 (Member 엔티티)
     * @param auction 경매 (Auction 엔티티)
     * @return 성공적으로 구매한 기록이 있다면 true, 아니면 false
     */
    @Query("SELECT CASE WHEN COUNT(ap) > 0 THEN true ELSE false END " +
            "FROM AuctionPayment ap " +
            "WHERE ap.bidder = :bidder " +
            "AND ap.auction = :auction " +
            "AND ap.status = com.creatorworks.nexus.auction.entity.PaymentStatus.SUCCESS")
    boolean hasSuccessfulPayment(@Param("bidder") Member bidder, @Param("auction") Auction auction);
}