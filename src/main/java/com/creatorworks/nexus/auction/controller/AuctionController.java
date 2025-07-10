package com.creatorworks.nexus.auction.controller;

import java.security.Principal;
import java.util.List;

import com.creatorworks.nexus.auction.dto.AuctionPageResponse;
import com.creatorworks.nexus.config.CategoryConfig;
import com.creatorworks.nexus.product.dto.ProductPageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.creatorworks.nexus.auction.dto.AuctionSaveRequest;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.service.AuctionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auctions")
public class AuctionController {
    private final AuctionService auctionService;
    private final CategoryConfig categoryConfig;

    @GetMapping("")
    public String auctionPage() {
        return "auction";
    }

    @GetMapping("/auctions/{id}")
    public String auctionDetail(@PathVariable Long id, Model model) {
        Auction auction = auctionService.findAuctionById(id);
        model.addAttribute("auction", auction);
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
    @GetMapping("/category/{categoryName}")
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

        return "product/auction_grid";
    }

    /**
     * 카테고리별 상품 목록 데이터를 JSON 형태로 반환하는 API 엔드포인트입니다.
     * @param primaryCategory 1차 카테고리
     * @param secondaryCategory 2차 카테고리 (필수 아님)
     * @param pageable 페이징 정보
     * @return 페이징 처리된 상품 데이터
     */
}
