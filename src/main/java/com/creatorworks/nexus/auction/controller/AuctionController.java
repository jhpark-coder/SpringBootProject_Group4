package com.creatorworks.nexus.auction.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.creatorworks.nexus.auction.dto.AuctionPageResponse;
import com.creatorworks.nexus.auction.dto.BiddingRequestDto;
import com.creatorworks.nexus.auction.dto.BiddingResponseDto;
import com.creatorworks.nexus.auction.entity.AuctionInquiry;
import com.creatorworks.nexus.auction.entity.Bid;
import com.creatorworks.nexus.auction.service.AuctionInquiryService;
import com.creatorworks.nexus.auction.service.BiddingService;
import com.creatorworks.nexus.auction.service.RecentlyViewedAuctionRedisService;
import com.creatorworks.nexus.config.CategoryConfig;
import com.creatorworks.nexus.member.dto.CustomUserDetails;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.service.MemberFollowService;
import com.creatorworks.nexus.util.tiptap.TipTapDocument;
import com.creatorworks.nexus.util.tiptap.TipTapNode;
import com.creatorworks.nexus.util.tiptap.TipTapRenderer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.creatorworks.nexus.auction.dto.AuctionSaveRequest;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.service.AuctionService;
import com.creatorworks.nexus.auction.repository.BidRepository;
import com.creatorworks.nexus.auction.dto.AuctionDetailViewDto;
import com.creatorworks.nexus.auction.dto.ReactBiddingDataDto;
import com.creatorworks.nexus.auction.dto.AuctionDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuctionController {

    private final AuctionService auctionService;
    private final CategoryConfig categoryConfig;
    private final AuctionInquiryService auctionInquiryService;
    private final MemberRepository memberRepository;
    private final MemberFollowService memberFollowService;
    private final RecentlyViewedAuctionRedisService recentlyViewedAuctionRedisService;
    private final RedisTemplate<String, String>redisTemplate;
    private final ObjectMapper objectMapper;
    private final TipTapRenderer tipTapRenderer;
    private final BiddingService biddingService; // 새로 추가된 서비스
    private final BidRepository bidRepository;

    @GetMapping("/auctions/{id}")
    public String auctionDetail(@PathVariable("id") Long id,
                                @Qualifier("inquiryPageable") @PageableDefault(size = 4, sort = "regTime", direction = Sort.Direction.DESC) Pageable inquiryPageable,
                                @RequestParam(value = "reviewKeyword", required = false) String reviewKeyword,
                                @RequestParam(value = "inquiryKeyword", required = false) String inquiryKeyword,
                                Principal principal,
                                Model model) {
        Auction auction = auctionService.findAuctionByIdAndIncrementView(id);

        // 문의 관련 (검색 기능 추가)
        Page<AuctionInquiry> inquiryPage = auctionInquiryService.findInquiriesByAuction(id, inquiryKeyword, inquiryPageable);

        // 현재 로그인한 사용자 정보 및 후기 작성 상태 조회
        Member currentMember = null;
        boolean canWriteReview = false;
        boolean canViewContent = false; // 컨텐츠 열람 가능 여부
        boolean isFollowing = false; // 팔로우 상태
        Long highestBidAmount = null;

        if (principal != null) {
            currentMember = memberRepository.findByEmail(principal.getName());
            if(currentMember != null) {

                // --- 팔로우 상태 확인 로직 추가 ---
                if (auction.getSeller() != null) {
                    isFollowing = memberFollowService.isFollowing(currentMember.getId(), auction.getSeller().getId());
                }
                // ---

                // ==============================================================
                //      ★★★  20250701 최근 본 상품을 위한 로직 추가 ★★★
                // ==============================================================
                // --- ★★★ Redis 기록 로직 전체를 아래 코드로 교체 ★★★ ---
                // 1. 개인별 최근 본 상품 기록 (서비스 사용)
                recentlyViewedAuctionRedisService.addAuctionToHistory(currentMember.getId(), id);
                log.info("사용자 ID {}의 최근 본 상품 기록 추가 (서비스 호출): 상품 ID {}", currentMember.getId(), id);

                // 2. 카테고리별 조회수 통계 기록 (새로운 로직)
                if (auction.getPrimaryCategory() != null && auction.getSecondaryCategory() != null) {
                    // 오늘 날짜 키 (예: "categoryViewCount:2025-07-01")
                    String dailyCountKey = "categoryViewCount:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    // "primary:secondary" 형태의 멤버 (예: "artwork:포토그래피")
                    String categoryMember = auction.getPrimaryCategory() + ":" + auction.getSecondaryCategory();

                    // 해당 카테고리 멤버의 점수를 1 증가시킴
                    redisTemplate.opsForZSet().incrementScore(dailyCountKey, categoryMember, 1);
                    // 7일이 지난 데이터는 자동으로 사라지도록 TTL(Time To Live) 설정
                    redisTemplate.expire(dailyCountKey, 8, TimeUnit.DAYS);

                    log.info("카테고리 조회수 증가: Key='{}', Member='{}'", dailyCountKey, categoryMember);
                }
                // =======================================================

                canWriteReview = auctionService.hasUserPurchasedAuction(currentMember, auction);
                // 2. 판매자 본인인지 확인 (ID를 직접 비교하여 안정성 확보)
                boolean isSeller = auction.getSeller() != null && currentMember.getId().equals(auction.getSeller().getId());

                // 3. 컨텐츠 열람 권한 설정 (구매자 또는 관리자 또는 판매자)
                canViewContent = canWriteReview || isSeller || currentMember.getRole() == com.creatorworks.nexus.member.constant.Role.ADMIN;
            }
        }

        // [추가] 최고 입찰가 조회 로직
        Bid highestBid = bidRepository.findTopByAuctionOrderByAmountDesc(auction).orElse(null);
        if (highestBid != null) {
            highestBidAmount = highestBid.getAmount();
        }

        // Tiptap JSON 컨텐츠 처리
        String contentHtml = "";
        boolean hasPaywall = false; // 페이월 존재 여부 확인
        String tiptapJson = auction.getTiptapJson();
        if (tiptapJson != null && !tiptapJson.isEmpty()) {
            try {
                log.info("Processing tiptapJson for auction ID {}: {}", id, tiptapJson);
                TipTapDocument document = objectMapper.readValue(tiptapJson, TipTapDocument.class);
                List<TipTapNode> nodesToRender = document.getContent();

                // 페이월 노드가 있는지 확인
                for (TipTapNode node : nodesToRender) {
                    if ("paywall".equals(node.getType())) {
                        hasPaywall = true;
                        break;
                    }
                }

                if (!canViewContent && hasPaywall) { // 페이월이 있고 구매하지 않은 경우에만 콘텐츠 제한
                    int paywallIndex = -1;
                    for (int i = 0; i < nodesToRender.size(); i++) {
                        if ("paywall".equals(nodesToRender.get(i).getType())) {
                            paywallIndex = i;
                            break;
                        }
                    }
                    if (paywallIndex != -1) {
                        // subList가 반환하는 view 대신 새로운 리스트를 생성하여 안정성을 높입니다.
                        nodesToRender = new ArrayList<>(nodesToRender.subList(0, paywallIndex));
                    }
                }

                contentHtml = tipTapRenderer.render(nodesToRender);

            } catch (JsonProcessingException e) {
                // JSON 파싱 실패 시 로깅 또는 예외 처리
                // 여기서는 간단히 빈 문자열로 대체
                contentHtml = "<p>콘텐츠를 불러오는 데 실패했습니다.</p>";
            }
        }

        // --- 태그 정보 추가 ---
        List<String> allTagNames = auction.getItemTags().stream()
                .map(auctionItemTag -> auctionItemTag.getItemTag().getName())
                .toList();

        // 카테고리를 제외한 순수 태그만 필터링
        List<String> pureTagNames = allTagNames.stream()
                .filter(tagName -> !tagName.equals(auction.getPrimaryCategory()))
                .filter(tagName -> !tagName.equals(auction.getSecondaryCategory()))
                .toList();

        // --- [수정] DTO를 사용한 모델 데이터 조립 ---

        // 1. Auction 엔티티를 기본 정보만 담은 AuctionDto로 변환합니다.
        AuctionDto auctionDto = new AuctionDto(auction);

        // 2. React 앱에 전달할 데이터를 별도의 DTO로 만듭니다.
        String jwtToken = null; // 실제 JWT 토큰 추출 로직 필요
        ReactBiddingDataDto biddingData = new ReactBiddingDataDto(
                auctionDto.id(),
                (highestBidAmount != null) ? highestBidAmount : auctionDto.startBidPrice(),
                auctionDto.buyNowPrice(),
                auctionDto.auctionEndTime(),
                jwtToken
        );

        // 3. 화면 전체에 필요한 모든 정보를 최종 뷰 DTO에 담습니다.
        AuctionDetailViewDto auctionDetailView = new AuctionDetailViewDto(
                auctionDto,
                contentHtml,
                hasPaywall,
                canViewContent,
                isFollowing,
                currentMember,
                biddingData
        );

        model.addAttribute("viewData", auctionDetailView);
        model.addAttribute("inquiryPage", inquiryPage);
        model.addAttribute("inquiryKeyword", inquiryKeyword);
        model.addAttribute("reviewKeyword", reviewKeyword); // 필요하다면 유지

        model.addAttribute("tagNames", pureTagNames);

        // 카테고리 및 태그 디버그 로그
        log.debug("🔍 상품 {} 정보:", id);
        log.debug("  - primaryCategory: '{}'", auction.getPrimaryCategory());
        log.debug("  - secondaryCategory: '{}'", auction.getSecondaryCategory());
        log.debug("  - 전체 태그 목록: {}", allTagNames);
        log.debug("  - 순수 태그 목록: {}", pureTagNames);
        // ---
        return "auction/auctionDetail";
    }



    @PostMapping("/api/auctions")
    @ResponseBody
    public Long saveAuction(@RequestBody AuctionSaveRequest request, Principal principal) {
        String userEmail = principal.getName();
        Auction saved = auctionService.saveAuction(request, userEmail);
        return saved.getId();
    }

    @PutMapping("/api/auctions/{id}")
    @ResponseBody
    public Long updateAuction(@PathVariable Long id, @RequestBody AuctionSaveRequest request, Principal principal) {
        String userEmail = principal.getName();
        Auction updated = auctionService.updateAuction(id, request, userEmail);
        return updated.getId();
    }

    @GetMapping("/api/auctions/{id}")
    @ResponseBody
    public Auction getAuction(@PathVariable Long id) {
        return auctionService.findAuctionById(id);
    }

    @GetMapping("/result/auction/{id}")
    public String auctionResult(@PathVariable Long id, Model model) {
        Auction auction = auctionService.findAuctionById(id);
        model.addAttribute("auction", auction);
        return "auction/auctionResult";
    }

    /**
     * 카테고리별 상품 그리드 뷰 페이지를 렌더링합니다.
     * @param categoryName 1차 카테고리 이름 (예: "artwork", "java")
     * @param page URL로 전달되는 페이지 번호 (1부터 시작)
     * @param secondaryCategory 2차 카테고리 이름 (필수 아님)
     * @param model 뷰에 데이터를 전달하기 위한 모델 객체
     * @return 렌더링할 뷰의 이름 ("product/category_grid")
     */
    @GetMapping("/auctions/category/{categoryName}")
    public String categoryGridView(@PathVariable String categoryName,
                                   @RequestParam(value = "page", defaultValue = "1") int page,
                                   @RequestParam(value = "secondary", defaultValue = "all") String secondaryCategory,
                                   Model model) {

        // 1. 초기 상품 데이터 로드 (페이지의 첫 16개)
        // URL의 page 파라미터(uiPage)는 1부터 시작, 100개 단위. API 페이지는 16개 단위.
        long itemsPerApiPage = 16;
        long apiPagesPerUiPage = 6; // 16 * 6 = 96개, 약 100개
        long initialApiPage = (page - 1) * apiPagesPerUiPage;

        Pageable initialPageable = PageRequest.of((int)initialApiPage, (int)itemsPerApiPage, Sort.by(Sort.Direction.DESC, "regTime"));
        AuctionPageResponse initialAuctionPage = auctionService.findAllAuctions(categoryName, secondaryCategory, initialPageable);

        // 2. 전체 2차 카테고리 목록 조회 (버튼용)
        List<String> secondaryCategories = categoryConfig.getSecondaryCategories(categoryName);

        // 3. 페이지네이션 계산 (전체 아이템 수 / 페이지당 아이템 100개)
        long totalItems = initialAuctionPage.getTotalElements();
        long itemsPerUiPage = itemsPerApiPage * apiPagesPerUiPage;
        long totalPages = (totalItems == 0) ? 1 : (long) Math.ceil((double) totalItems / itemsPerUiPage);

        // 4. 뷰에 데이터 전달
        model.addAttribute("primaryCategory", categoryName);
        model.addAttribute("secondaryCategory", secondaryCategory); // 현재 선택된 2차 카테고리
        model.addAttribute("secondaryCategories", secondaryCategories);
        model.addAttribute("initialAuctionPage", initialAuctionPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "auction/auction_grid";
    }
    /**
     * 특정 경매에 대한 입찰을 처리하는 API
     * @param auctionId 경매 ID
     * @param requestDto 입찰 요청 정보 (금액)
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 처리 결과 (성공/실패, 메시지, 새로운 최고가)
     */
    @PostMapping("/api/auctions/{auctionId}/bids")

    public ResponseEntity<BiddingResponseDto> placeBid(
            @PathVariable Long auctionId,
            @RequestBody @Valid BiddingRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            // BiddingService를 통해 입찰 처리
            Bid newBid = biddingService.placeBid(auctionId, requestDto, userDetails.getName());

            // 성공 응답 생성
            BiddingResponseDto response = new BiddingResponseDto(
                    true,
                    "입찰에 성공했습니다.",
                    newBid.getAmount(),
                    newBid.getBidder().getName() // 또는 getName()
            );
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            // 유효성 검사 실패 시
            BiddingResponseDto response = new BiddingResponseDto(false, e.getMessage(), null, null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            // 기타 예외 발생 시
            BiddingResponseDto response = new BiddingResponseDto(false, "입찰 처리 중 오류가 발생했습니다.", null, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
