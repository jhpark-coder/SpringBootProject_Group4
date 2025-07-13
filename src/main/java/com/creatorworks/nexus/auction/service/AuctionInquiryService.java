package com.creatorworks.nexus.auction.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.auction.dto.AuctionInquiryRequestDto;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.AuctionInquiry;
import com.creatorworks.nexus.auction.repository.AuctionInquiryRepository;
import com.creatorworks.nexus.auction.repository.AuctionRepository;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionInquiryService {

    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final AuctionInquiryRepository auctionInquiryRepository;

    @Transactional(readOnly = true)
    public Page<AuctionInquiry> findInquiriesByAuction(Long auctionId, String keyword, Pageable pageable) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid auction Id:" + auctionId));

        // 키워드가 없으면 DB에서 바로 페이징하여 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return auctionInquiryRepository.findByAuctionAndParentIsNull(auction, pageable);
        }

        // 키워드가 있으면, 전체 목록을 가져와 애플리케이션 레벨에서 필터링
        List<AuctionInquiry> allInquiries = auctionInquiryRepository.findByAuctionAndParentIsNull(auction, Pageable.unpaged()).getContent();

        List<AuctionInquiry> filteredInquiries = allInquiries.stream()
                .filter(inquiry -> inquiry.getContent() != null && inquiry.getContent().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        // 수동으로 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredInquiries.size());

        List<AuctionInquiry> pageContent = (start > filteredInquiries.size()) ? List.of() : filteredInquiries.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredInquiries.size());
    }

    public void createInquiry(Long auctionId, AuctionInquiryRequestDto requestDto, String userEmail) {
        // 상품과 작성자(회원) 엔티티를 조회합니다.
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. id=" + auctionId));

        Member writer = memberRepository.findByEmail(userEmail);
        if (writer == null) {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. email=" + userEmail);
        }

        // AuctionInquiry 엔티티를 생성합니다.
        AuctionInquiry inquiry = AuctionInquiry.builder()
                .auction(auction)
                .writer(writer)
                .content(requestDto.getContent())
                .isSecret(requestDto.getIsSecret() != null && requestDto.getIsSecret())
                .parent(null) // 최상위 문의이므로 부모는 null
                .build();

        // 생성된 문의를 저장합니다.
        auctionInquiryRepository.save(inquiry);
    }

    @Transactional
    public void createReply(Long auctionId, Long parentInquiryId, AuctionInquiryRequestDto requestDto, String userEmail) {
        // 상품, 작성자, 부모 문의 엔티티를 조회합니다.
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. id=" + auctionId));

        Member writer = memberRepository.findByEmail(userEmail);
        if (writer == null) {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. email=" + userEmail);
        }

        AuctionInquiry parent = auctionInquiryRepository.findById(parentInquiryId)
                .orElseThrow(() -> new IllegalArgumentException("부모 문의를 찾을 수 없습니다. id=" + parentInquiryId));

        // 권한 검증: 현재 로그인한 사용자가 상품의 판매자인지 확인합니다.
        if (!auction.getSeller().getId().equals(writer.getId())) {
            throw new IllegalStateException("답변을 작성할 권한이 없습니다.");
        }

        // AuctionInquiry 엔티티(답변)를 생성합니다.
        AuctionInquiry reply = AuctionInquiry.builder()
                .auction(auction)
                .writer(writer)
                .content(requestDto.getContent())
                .isSecret(requestDto.getIsSecret() != null && requestDto.getIsSecret())
                .parent(parent) // 부모 문의 설정
                .build();

        auctionInquiryRepository.save(reply);
    }
} 