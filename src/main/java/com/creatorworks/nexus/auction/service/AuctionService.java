package com.creatorworks.nexus.auction.service;

import java.time.LocalDateTime;
import java.util.List;

import com.creatorworks.nexus.auction.Specification.AuctionSpecification;
import com.creatorworks.nexus.auction.dto.AuctionDto;
import com.creatorworks.nexus.auction.dto.AuctionPageResponse;
import com.creatorworks.nexus.auction.entity.Bid;
import com.creatorworks.nexus.auction.repository.AuctionPaymentRepository;
import com.creatorworks.nexus.auction.repository.BidRepository;
import com.creatorworks.nexus.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.auction.dto.AuctionSaveRequest;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.AuctionItemTag;
import com.creatorworks.nexus.auction.repository.AuctionItemTagRepository;
import com.creatorworks.nexus.auction.repository.AuctionRepository;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.entity.ItemTag;
import com.creatorworks.nexus.product.repository.ItemTagRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final ItemTagRepository itemTagRepository;
    private final AuctionItemTagRepository auctionItemTagRepository;
    private final AuctionPaymentRepository auctionPaymentRepository;
    private final BidRepository bidRepository;

    @Transactional
    public Auction saveAuction(AuctionSaveRequest request, String userEmail) {
        Member seller = memberRepository.findByEmail(userEmail);
        if (seller == null) {
            throw new IllegalArgumentException("작성자 정보를 찾을 수 없습니다: " + userEmail);
        }

        Auction auction = Auction.builder()
                .seller(seller)
                .name(request.getName())
                .startBidPrice(request.getStartBidPrice())
                .buyNowPrice(request.getBuyNowPrice())
                .auctionEndTime(request.getAuctionEndTime())
                .description(request.getDescription())
                .workDescription(request.getWorkDescription())
                .tiptapJson(request.getTiptapJson())
                .imageUrl(request.getImageUrl())
                .primaryCategory(request.getPrimaryCategory())
                .secondaryCategory(request.getSecondaryCategory())
                .backgroundColor(request.getBackgroundColor())
                .fontFamily(request.getFontFamily())
                .build();
        
        Auction savedAuction = auctionRepository.save(auction);
        saveTags(savedAuction, request.getTags());
        return savedAuction;
    }

    @Transactional
    public Auction updateAuction(Long id, AuctionSaveRequest request, String userEmail) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid auction Id:" + id));

        if (!auction.getSeller().getEmail().equals(userEmail)) {
            throw new IllegalStateException("경매를 수정할 권한이 없습니다.");
        }

        auction.setName(request.getName());
        auction.setStartBidPrice(request.getStartBidPrice());
        auction.setBuyNowPrice(request.getBuyNowPrice());
        auction.setAuctionEndTime(request.getAuctionEndTime());
        auction.setDescription(request.getDescription());
        auction.setTiptapJson(request.getTiptapJson());
        auction.setImageUrl(request.getImageUrl());
        auction.setWorkDescription(request.getWorkDescription());
        auction.setPrimaryCategory(request.getPrimaryCategory());
        auction.setSecondaryCategory(request.getSecondaryCategory());
        auction.setBackgroundColor(request.getBackgroundColor());
        auction.setFontFamily(request.getFontFamily());

        auctionItemTagRepository.deleteAllByAuctionId(auction.getId());
        saveTags(auction, request.getTags());

        return auction; // 더티 체킹으로 업데이트
    }

    public Auction findAuctionById(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid auction Id:" + id));
    }

    private void saveTags(Auction auction, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        auctionItemTagRepository.deleteAllByAuctionId(auction.getId());

        for (String tagName : tagNames) {
            ItemTag itemTag = itemTagRepository.findByName(tagName)
                    .orElseGet(() -> itemTagRepository.save(ItemTag.builder().name(tagName).build()));

            AuctionItemTag auctionItemTag = AuctionItemTag.builder()
                    .auction(auction)
                    .itemTag(itemTag)
                    .build();

            auctionItemTagRepository.save(auctionItemTag);
        }
    }

    public AuctionPageResponse findAllAuctions(String primaryCategory, String secondaryCategory, Pageable pageable){
        Specification<Auction> spec = Specification.where(AuctionSpecification.byCategory(primaryCategory, secondaryCategory));
        Page<Auction> auctionPage = auctionRepository.findAll(spec, pageable);

        List<AuctionDto> auctionDtos = auctionPage.getContent().stream()
                .map(AuctionDto::new)
                .toList();

        return new AuctionPageResponse(
                auctionDtos,
                auctionPage.getNumber(),
                auctionPage.getTotalPages(),
                auctionPage.getTotalElements(),
                auctionPage.getSize(),
                auctionPage.isFirst(),
                auctionPage.isLast()
        );

    }
    /**
     * 상품 조회와 동시에 조회수를 증가시킵니다. (상품 상세 페이지 조회 시 사용)
     * @param id 조회할 상품의 ID.
     * @return 찾아낸 상품(Product) 객체.
     * @throws IllegalArgumentException 해당 ID의 상품이 존재하지 않을 경우 예외를 발생시킵니다.
     */
    @Transactional
    public Auction findAuctionByIdAndIncrementView(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid auction Id:" + id));
        auction.setViewCount(auction.getViewCount() + 1);
        return auction;
    }

    @Transactional(readOnly = true)
    public boolean hasUserPurchasedAuction(Member member, Auction auction) {
        if (member == null || auction == null) {
            return false;
        }
        // OrderRepository를 사용하여 구매 이력 확인
        return auctionPaymentRepository.hasSuccessfulPayment(member, auction);
    }

    /**
     * 경매에 입찰을 처리하는 메서드
     * @param auctionId 경매 ID
     * @param bidPrice 입찰 가격
     * @param userEmail 입찰자 이메일
     * @throws EntityNotFoundException 경매를 찾을 수 없는 경우
     * @throws IllegalArgumentException 입찰 조건이 맞지 않는 경우
     */
    @Transactional
    public void placeBid(Long auctionId, Long bidPrice, String userEmail) {
        // 1. 경매 존재 여부 확인
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("경매를 찾을 수 없습니다: " + auctionId));

        // 2. 입찰자 정보 확인
        Member bidder = memberRepository.findByEmail(userEmail);
        if (bidder == null) {
            throw new EntityNotFoundException("입찰자 정보를 찾을 수 없습니다: " + userEmail);
        }

        // 3. 경매 종료 여부 확인
        if (auction.getAuctionEndTime() != null && LocalDateTime.now().isAfter(auction.getAuctionEndTime())) {
            throw new IllegalArgumentException("이미 종료된 경매입니다.");
        }

        // 4. 판매자 본인 입찰 방지
        if (auction.getSeller().getId().equals(bidder.getId())) {
            throw new IllegalArgumentException("자신이 등록한 경매에는 입찰할 수 없습니다.");
        }

        // 5. 입찰가 유효성 검사
        Long currentPrice = auction.getCurrentPrice() != null ? auction.getCurrentPrice() : auction.getStartBidPrice();
        if (bidPrice <= currentPrice) {
            throw new IllegalArgumentException("입찰가는 현재가(" + currentPrice + "원)보다 높아야 합니다.");
        }

        // 6. 입찰 기록 저장
        Bid bid = Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .price(bidPrice)
                .build();
        bidRepository.save(bid);

        // 7. 경매 정보 업데이트 (현재가, 최고 입찰자)
        auction.setCurrentPrice(bidPrice);
        auction.setHighestBidder(bidder);
        auctionRepository.save(auction);

        // 8. WebSocket을 통한 실시간 업데이트 (선택사항)
        // bidWebSocketHandler.broadcastPriceUpdate(auctionId, bidPrice, bidder.getName());
    }

    /**
     * 특정 사용자가 입찰한 경매 목록을 조회합니다.
     * @param bidder 입찰자
     * @param pageable 페이징 정보
     * @return 입찰한 경매 목록
     */
    public Page<Auction> findAuctionsByBidder(Member bidder, Pageable pageable) {
        return bidRepository.findAuctionsByBidder(bidder, pageable);
    }
}