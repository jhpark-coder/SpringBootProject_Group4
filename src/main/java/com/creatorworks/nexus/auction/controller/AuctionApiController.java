package com.creatorworks.nexus.auction.controller;

import com.creatorworks.nexus.auction.dto.AuctionPageResponse;
import com.creatorworks.nexus.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController // <<-- 가장 중요! @Controller가 아닙니다.
@RequiredArgsConstructor
@RequestMapping("/api/auctions") // <<-- Javascript가 원하는 바로 그 주소
public class AuctionApiController {

    private final AuctionService auctionService;

    @GetMapping("/category") // /api/auctions 뒤에 /category가 붙습니다.
    public AuctionPageResponse getAuctionsByCategoryApi( // 메소드 이름 변경 (혼동 방지)
                                                         @RequestParam("primary") String primaryCategory,
                                                         @RequestParam(value = "secondary", required = false, defaultValue = "all") String secondaryCategory,
                                                         Pageable pageable) {
        return auctionService.findAllAuctions(primaryCategory, secondaryCategory, pageable);
    }

    // 다른 API들도 이곳으로 옮기면 좋습니다.
    // 예: @PostMapping, @PutMapping, @GetMapping("/{id}") 등
}