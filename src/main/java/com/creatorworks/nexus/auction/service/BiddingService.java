package com.creatorworks.nexus.auction.service;

import com.creatorworks.nexus.auction.dto.BiddingRequestDto;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.Bid;
import com.creatorworks.nexus.auction.repository.AuctionRepository;
import com.creatorworks.nexus.auction.repository.BidRepository;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BiddingService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public synchronized Bid placeBid(Long auctionId, BiddingRequestDto requestDto, String userEmail) {
        // 1. 사용자(입찰자)와 경매 정보 조회 (findByEmail 사용 및 null 체크)
        Member bidder = memberRepository.findByEmail(userEmail);
        if (bidder == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다. (email: " + userEmail + ")");
        }

        // JpaRepository의 findById는 기본적으로 Optional을 반환하므로, 이 부분은 .orElse(null)로 처리합니다.
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null) {
            throw new IllegalArgumentException("경매를 찾을 수 없습니다. (id: " + auctionId + ")");
        }

        // 2. 입찰 유효성 검사
        validateBid(auction, requestDto.getAmount(), bidder);

        // 3. 새로운 입찰(Bid) 객체 생성 및 저장
        Bid newBid = Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .amount(requestDto.getAmount())
                .build();

        return bidRepository.save(newBid);
    }

    private void validateBid(Auction auction, Long bidAmount, Member bidder) {
        // ... (이 메소드의 내용은 이전과 동일합니다) ...
        if (LocalDateTime.now().isAfter(auction.getAuctionEndTime())) {
            throw new IllegalStateException("이미 종료된 경매입니다.");
        }
        if (auction.getSeller().getId().equals(bidder.getId())) {
            throw new IllegalStateException("자신의 경매에는 입찰할 수 없습니다.");
        }
        if (auction.getBuyNowPrice() != null && bidAmount > auction.getBuyNowPrice()) {
            throw new IllegalArgumentException("입찰가는 즉시 구매가를 초과할 수 없습니다.");
        }
        Bid highestBid = bidRepository.findTopByAuctionOrderByAmountDesc(auction).orElse(null);
        if (highestBid != null) {
            if (bidAmount <= highestBid.getAmount()) {
                throw new IllegalArgumentException("입찰가는 현재 최고가보다 높아야 합니다.");
            }
        } else {
            if (bidAmount < auction.getStartBidPrice()) {
                throw new IllegalArgumentException("첫 입찰가는 시작가보다 높거나 같아야 합니다.");
            }
        }
    }
}