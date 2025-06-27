package com.creatorworks.nexus.product.controller;

import com.creatorworks.nexus.product.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    // 찜한 상품 목록 페이지
    @GetMapping
    public String wishlistPage(Model model) {
        // TODO: 실제 로그인된 사용자 정보를 가져와야 함
        Long memberId = 1L; // 임시로 1L 사용
        
        model.addAttribute("wishedProducts", wishlistService.getWishedProducts(memberId));
        model.addAttribute("wishlistCount", wishlistService.getWishlistCount(memberId));
        return "product/wishlist";
    }

    // 찜하기 추가 API
    @PostMapping("/add/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToWishlist(@PathVariable Long productId) {
        // TODO: 실제 로그인된 사용자 정보를 가져와야 함
        Long memberId = 1L; // 임시로 1L 사용
        
        Map<String, Object> response = new HashMap<>();
        
        boolean success = wishlistService.addToWishlist(memberId, productId);
        
        if (success) {
            response.put("success", true);
            response.put("message", "찜하기에 추가되었습니다.");
            response.put("wishCount", wishlistService.getProductWishCount(productId));
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "이미 찜한 상품입니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 찜하기 제거 API
    @DeleteMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromWishlist(@PathVariable Long productId) {
        // TODO: 실제 로그인된 사용자 정보를 가져와야 함
        Long memberId = 1L; // 임시로 1L 사용
        
        Map<String, Object> response = new HashMap<>();
        
        boolean success = wishlistService.removeFromWishlist(memberId, productId);
        
        if (success) {
            response.put("success", true);
            response.put("message", "찜하기에서 제거되었습니다.");
            response.put("wishCount", wishlistService.getProductWishCount(productId));
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "찜하지 않은 상품입니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 찜하기 토글 API (찜하기/찜해제)
    @PostMapping("/toggle/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(@PathVariable Long productId) {
        // TODO: 실제 로그인된 사용자 정보를 가져와야 함
        Long memberId = 1L; // 임시로 1L 사용
        
        Map<String, Object> response = new HashMap<>();
        
        boolean success = wishlistService.toggleWishlist(memberId, productId);
        
        if (success) {
            boolean isWished = wishlistService.isWished(memberId, productId);
            response.put("success", true);
            response.put("isWished", isWished);
            response.put("message", isWished ? "찜하기에 추가되었습니다." : "찜하기에서 제거되었습니다.");
            response.put("wishCount", wishlistService.getProductWishCount(productId));
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "찜하기 처리 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 찜하기 상태 확인 API
    @GetMapping("/check/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkWishlistStatus(@PathVariable Long productId) {
        // TODO: 실제 로그인된 사용자 정보를 가져와야 함
        Long memberId = 1L; // 임시로 1L 사용
        
        Map<String, Object> response = new HashMap<>();
        
        boolean isWished = wishlistService.isWished(memberId, productId);
        long wishCount = wishlistService.getProductWishCount(productId);
        
        response.put("success", true);
        response.put("isWished", isWished);
        response.put("wishCount", wishCount);
        
        return ResponseEntity.ok(response);
    }

    // 찜하기 개수 조회 API
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistCount() {
        // TODO: 실제 로그인된 사용자 정보를 가져와야 함
        Long memberId = 1L; // 임시로 1L 사용
        
        Map<String, Object> response = new HashMap<>();
        long count = wishlistService.getWishlistCount(memberId);
        
        response.put("success", true);
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
} 