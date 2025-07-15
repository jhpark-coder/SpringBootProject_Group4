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
import com.creatorworks.nexus.notification.entity.Notification;
import com.creatorworks.nexus.notification.repository.NotificationRepository;
import com.creatorworks.nexus.notification.entity.NotificationCategory;
import com.creatorworks.nexus.auction.handler.BidWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {
    private static final Logger log = LoggerFactory.getLogger(AuctionService.class);
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final ItemTagRepository itemTagRepository;
    private final AuctionItemTagRepository auctionItemTagRepository;
    private final AuctionPaymentRepository auctionPaymentRepository;
    private final BidRepository bidRepository;
    private final NotificationRepository notificationRepository;
    private final BidWebSocketHandler bidWebSocketHandler;

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
    public Bid placeBid(Long auctionId, Long bidPrice, String userEmail) {
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

        // 6. 즉시 구매가 검증
        if (auction.getBuyNowPrice() != null && bidPrice > auction.getBuyNowPrice()) {
            throw new IllegalArgumentException("입찰가는 즉시구매가(" + auction.getBuyNowPrice() + "원)보다 높을 수 없습니다.");
        }

        // 7. 입찰 기록 저장
        Bid bid = Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .price(bidPrice)
                .build();
        bidRepository.save(bid);

        // 8. 이전 최고 입찰자 정보 저장 (알림 전송용)
        Member previousHighestBidder = auction.getHighestBidder();

        // 9. 경매 정보 업데이트 (현재가, 최고 입찰자)
        auction.setCurrentPrice(bidPrice);
        auction.setHighestBidder(bidder);
        // @Transactional 덕분에 auctionRepository.save(auction)을 호출하지 않아도
        // 메소드가 끝날 때 변경된 내용이 자동으로 DB에 반영됩니다.

        log.info("입찰 성공! 경매 ID: {}, 입찰자: {}, 입찰가: {}", auctionId, bidder.getName(), bidPrice);

        // 10. 입찰 알림 전송 (이전 최고 입찰자에게)
        sendBidNotification(auction, bidder, bidPrice, previousHighestBidder);
        
        // 10-1. 입찰 성공 알림 전송 (입찰자 본인에게)
        sendBidSuccessNotification(auction, bidder, bidPrice);

        // 11. ★★★ 입찰 성공 후 웹소켓 방송 호출! ★★★
        bidWebSocketHandler.broadcastPriceUpdate(
                auction.getId(),
                auction.getCurrentPrice(),
                bidder.getName() // 최고 입찰자의 이름
        );

        log.info("입찰 성공! 웹소켓으로 가격 업데이트를 방송합니다.");

        return bid;
    }

    /**
     * 입찰 알림 전송
     */
    private void sendBidNotification(Auction auction, Member bidder, Long bidPrice, Member previousHighestBidder) {
        try {
            // 이전 최고 입찰자에게 알림 (본인이 아닌 경우)
            if (previousHighestBidder != null && 
                !previousHighestBidder.getId().equals(bidder.getId())) {
                
                String message = String.format("다른 사용자가 %s 경매에 %,d원으로 입찰했습니다.", 
                    auction.getName(), bidPrice);
                
                Notification notification = Notification.builder()
                        .senderUserId(0L) // 시스템 알림
                        .targetUserId(previousHighestBidder.getId())
                        .message(message)
                        .type("auction_bid")
                        .category(NotificationCategory.AUCTION)
                        .isRead(false)
                        .link("/auctions/" + auction.getId()) // 경로 수정
                        .build();
                
                notificationRepository.save(notification);
                
                log.info("입찰 알림 전송 완료: auctionId={}, targetUserId={}, bidPrice={}", 
                    auction.getId(), previousHighestBidder.getId(), bidPrice);
            }
        } catch (Exception e) {
            log.error("입찰 알림 전송 실패: auctionId={}, error={}", auction.getId(), e.getMessage());
        }
    }

    /**
     * 입찰 성공 알림 전송 (입찰자 본인에게)
     */
    private void sendBidSuccessNotification(Auction auction, Member bidder, Long bidPrice) {
        try {
            String message = String.format("축하합니다! %s 경매에 %,d원으로 입찰에 성공했습니다.", 
                auction.getName(), bidPrice);
            
            Notification notification = Notification.builder()
                    .senderUserId(0L) // 시스템 알림
                    .targetUserId(bidder.getId())
                    .message(message)
                    .type("auction_bid_success")
                    .category(NotificationCategory.AUCTION)
                    .isRead(false)
                    .link("/auctions/" + auction.getId())
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("입찰 성공 알림 전송 완료: auctionId={}, bidderId={}, bidPrice={}", 
                auction.getId(), bidder.getId(), bidPrice);
        } catch (Exception e) {
            log.error("입찰 성공 알림 전송 실패: auctionId={}, error={}", auction.getId(), e.getMessage());
        }
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

    /**
     * 특정 판매자가 등록한 경매 목록을 조회합니다.
     * @param userEmail 판매자 이메일
     * @param pageable 페이징 정보
     * @return 경매 목록 페이지
     */
    public Page<Auction> findAuctionsBySeller(String userEmail, Pageable pageable) {
        com.creatorworks.nexus.member.entity.Member seller = memberRepository.findByEmail(userEmail);
        if (seller == null) {
            throw new IllegalArgumentException("판매자 정보를 찾을 수 없습니다: " + userEmail);
        }
        return auctionRepository.findBySeller(seller, pageable);
    }
}