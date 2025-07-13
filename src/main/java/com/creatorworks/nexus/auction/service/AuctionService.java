package com.creatorworks.nexus.auction.service;

import java.util.List;

import com.creatorworks.nexus.auction.Specification.AuctionSpecification;
import com.creatorworks.nexus.auction.dto.AuctionDto;
import com.creatorworks.nexus.auction.dto.AuctionPageResponse;
import com.creatorworks.nexus.auction.entity.Bid;
import com.creatorworks.nexus.auction.handler.BidWebSocketHandler;
import com.creatorworks.nexus.auction.repository.AuctionPaymentRepository;
import com.creatorworks.nexus.auction.repository.BidRepository;
import com.creatorworks.nexus.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final ItemTagRepository itemTagRepository;
    private final AuctionItemTagRepository auctionItemTagRepository;
    private final AuctionPaymentRepository auctionPaymentRepository;
    private final BidRepository bidRepository; // <<< 추가: BidRepository를 사용하기 위해 주입받습니다.
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

    /**
     * 특정 사용자가 입찰에 참여한 경매 목록을 조회하는 서비스 메소드
     */
    @Transactional(readOnly = true) // 데이터를 변경하지 않는 조회 기능이므로 readOnly = true로 성능 최적화
    public Page<Auction> findBiddingAuctionsByUser(String userEmail, Pageable pageable) {
        // 1. 이메일로 사용자(Member) 정보를 찾습니다.
        Member bidder = memberRepository.findByEmail(userEmail);
        if (bidder == null) {
            // 사용자가 없으면 빈 페이지를 반환합니다.
            return Page.empty(pageable);
        }

        // 2. Repository에 요청해서 해당 사용자가 입찰한 경매 목록을 가져옵니다.
        return bidRepository.findAuctionsByBidder(bidder, pageable);
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
     * 입찰을 처리하는 핵심 메소드
     * @param auctionId 입찰할 경매의 ID
     * @param bidPrice 사용자가 제시한 입찰 가격
     * @param bidderEmail 입찰을 시도하는 사용자의 이메일
     * @return 생성된 입찰(Bid) 객체
     */
    @Transactional // 이 메소드 안의 DB 작업들은 하나의 묶음(트랜잭션)으로 처리돼요.
    public Bid placeBid(Long auctionId, Long bidPrice, String bidderEmail) {
        // 1. 입찰자(Member)와 경매(Auction) 정보를 DB에서 찾아옵니다.
        //    만약 정보가 없으면 예외를 발생시켜서 중단해요.
        Member bidder = memberRepository.findByEmail(bidderEmail);
        if (bidder == null) {
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다: " + bidderEmail);
        }

        // ★★★ 중요: findById를 사용하면 트랜잭션 내에서 영속성 관리가 되어,
        // 나중에 auction 객체의 값을 바꾸기만 해도 DB에 자동으로 반영(UPDATE)됩니다. (더티 체킹)
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("경매를 찾을 수 없습니다: " + auctionId));

        // 2. 입찰 유효성 검사 (가장 중요한 부분!)
        // 2-1. 자기 자신의 경매에는 입찰할 수 없어요.
        if (auction.getSeller().getId().equals(bidder.getId())) {
            throw new IllegalArgumentException("자신의 경매에는 입찰할 수 없습니다.");
        }

        // 2-2. 입찰가는 현재 최고가보다 높아야 해요.
        //      (auction.getCurrentPrice()가 null일 경우를 대비해 startBidPrice와 비교)
        Long currentPrice = auction.getCurrentPrice() != null ? auction.getCurrentPrice() : auction.getStartBidPrice();
        if (bidPrice <= currentPrice) {
            throw new IllegalArgumentException("입찰가는 현재가(" + currentPrice + "원)보다 높아야 합니다.");
        }

        // 2-3. 즉시 구매가가 있다면, 그보다는 낮거나 같게 입찰해야 해요.
        if (auction.getBuyNowPrice() != null && bidPrice > auction.getBuyNowPrice()) {
            throw new IllegalArgumentException("입찰가는 즉시구매가(" + auction.getBuyNowPrice() + "원)보다 높을 수 없습니다.");
        }

        // 3. 모든 검사를 통과했다면, 입찰 기록(Bid)을 생성하고 DB에 저장합니다.
        Bid newBid = Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .price(bidPrice)
                .build();
        bidRepository.save(newBid);

        // 4. 경매(Auction) 정보의 현재 최고가와 최고 입찰자를 업데이트합니다.
        auction.setCurrentPrice(bidPrice);
        auction.setHighestBidder(bidder);
        // @Transactional 덕분에 auctionRepository.save(auction)을 호출하지 않아도
        // 메소드가 끝날 때 변경된 내용이 자동으로 DB에 반영됩니다.

        log.info("입찰 성공! 경매 ID: {}, 입찰자: {}, 입찰가: {}", auctionId, bidder.getName(), bidPrice);

        // 5. 생성된 입찰 정보를 반환합니다. (컨트롤러에서 이 정보를 활용할 수 있어요)

        // ★★★ 입찰 성공 후 웹소켓 방송 호출! (맨 아래 추가) ★★★
        bidWebSocketHandler.broadcastPriceUpdate(
                auction.getId(),
                auction.getCurrentPrice(),
                bidder.getName() // 최고 입찰자의 이름
        );

        log.info("입찰 성공! 웹소켓으로 가격 업데이트를 방송합니다.");

        return newBid;
    }

    /**
     * 특정 판매자가 등록한 경매 목록을 조회하는 서비스 메소드
     */
    @Transactional(readOnly = true)
    public Page<Auction> findAuctionsBySeller(String userEmail, Pageable pageable) {
        // 1. 이메일로 판매자(Member) 정보를 찾습니다.
        Member seller = memberRepository.findByEmail(userEmail);
        if (seller == null) {
            return Page.empty(pageable); // 사용자가 없으면 빈 페이지 반환
        }

        // 2. Repository를 호출하여 해당 판매자의 경매 목록을 가져옵니다.
        return auctionRepository.findBySeller(seller, pageable);
    }
}