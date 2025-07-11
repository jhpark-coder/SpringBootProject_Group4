package com.creatorworks.nexus.auction.service;

import java.util.List;

import com.creatorworks.nexus.auction.Specification.AuctionSpecification;
import com.creatorworks.nexus.auction.dto.AuctionDto;
import com.creatorworks.nexus.auction.dto.AuctionPageResponse;
import com.creatorworks.nexus.auction.repository.AuctionPaymentRepository;
import com.creatorworks.nexus.order.repository.OrderRepository;
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
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final ItemTagRepository itemTagRepository;
    private final AuctionItemTagRepository auctionItemTagRepository;
    private final AuctionPaymentRepository auctionPaymentRepository;

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
}