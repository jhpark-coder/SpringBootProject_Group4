package com.creatorworks.nexus.auction.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.auction.dto.AuctionPaymentRequest;
import com.creatorworks.nexus.auction.dto.AuctionPaymentResponse;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.service.AuctionPaymentService;
import com.creatorworks.nexus.auction.service.AuctionService;
import com.creatorworks.nexus.member.dto.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/auction-payment")
@RequiredArgsConstructor
@Slf4j
public class AuctionPaymentController {

    private final AuctionPaymentService auctionPaymentService;
    private final AuctionService auctionService;

    /**
     * 경매 결제 처리 API
     */
    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<AuctionPaymentResponse> processPayment(
            @RequestBody AuctionPaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            AuctionPaymentResponse response = auctionPaymentService.processPayment(
                    request, userDetails.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("경매 결제 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 결제 실패 처리 API
     */
    @PostMapping("/fail")
    @ResponseBody
    public ResponseEntity<Void> processPaymentFailure(
            @RequestParam String impUid,
            @RequestParam String failureReason) {

        try {
            auctionPaymentService.processPaymentFailure(impUid, failureReason);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("결제 실패 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 결제 취소 API
     */
    @PostMapping("/cancel")
    @ResponseBody
    public ResponseEntity<Void> cancelPayment(
            @RequestParam String impUid,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            auctionPaymentService.cancelPayment(impUid);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("결제 취소 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 사용자별 결제 내역 조회 API
     */
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<Page<AuctionPaymentResponse>> getUserPaymentHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuctionPaymentResponse> history = auctionPaymentService.getUserPaymentHistory(
                    userDetails.getUsername(), pageable);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("결제 내역 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 경매별 결제 내역 조회 API
     */
    @GetMapping("/auction/{auctionId}/history")
    @ResponseBody
    public ResponseEntity<List<AuctionPaymentResponse>> getAuctionPaymentHistory(
            @PathVariable Long auctionId) {

        try {
            List<AuctionPaymentResponse> history = auctionPaymentService.getAuctionPaymentHistory(auctionId);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("경매 결제 내역 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 결제 상세 정보 조회 API
     */
    @GetMapping("/{paymentId}")
    @ResponseBody
    public ResponseEntity<AuctionPaymentResponse> getPaymentDetail(@PathVariable Long paymentId) {

        try {
            AuctionPaymentResponse detail = auctionPaymentService.getPaymentDetail(paymentId);
            return ResponseEntity.ok(detail);

        } catch (Exception e) {
            log.error("결제 상세 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 사용자별 결제 통계 API
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<AuctionPaymentService.PaymentStatistics> getUserPaymentStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            AuctionPaymentService.PaymentStatistics statistics =
                    auctionPaymentService.getUserPaymentStatistics(userDetails.getUsername());
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("결제 통계 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 경매 결제 페이지
     */
    @GetMapping("/payment-page/{auctionId}")
    public String showPaymentPage(@PathVariable Long auctionId, Model model) {
        // [추가된 로직] 경매 ID로 경매 정보를 서버에서 직접 조회합니다.
        Auction auction = auctionService.findAuctionById(auctionId);

        // [추가된 로직] 조회된 정보에서 '즉시 입찰가'를 가져옵니다.
        Long price = auction.getBuyNowPrice();

        // [안전장치] 만약 즉시 입찰가가 없거나, 조회된 경매가 없다면 원래 상품 페이지로 돌려보냅니다.
        if (auction == null || price == null) {
            log.warn("잘못된 결제 시도: 경매 ID {}에 대한 즉시 입찰 정보가 없습니다.", auctionId);
            return "redirect:/auctions/" + auctionId; // 경매 상세 페이지 주소로 변경
        }

        // 모델에 경매 ID와 '서버에서 직접 조회한 안전한 가격'을 담습니다.
        model.addAttribute("auctionId", auctionId);
        model.addAttribute("price", price); // price 정보를 추가로 담아줍니다.

        // 결제 페이지(auction/payment.html)로 이동합니다.
        return "auction/payment";
    }

    /**
     * 경매 결제 성공 페이지
     */
    @GetMapping("/success")
    public String showPaymentSuccessPage(@RequestParam String impUid, Model model) {
        model.addAttribute("impUid", impUid);
        return "auction/paymentSuccess";
    }

    /**
     * 경매 결제 실패 페이지
     */
    @GetMapping("/fail")
    public String showPaymentFailPage(@RequestParam String impUid,
                                      @RequestParam String failureReason,
                                      Model model) {
        model.addAttribute("impUid", impUid);
        model.addAttribute("failureReason", failureReason);
        return "auction/paymentFail";
    }

    /**
     * 경매 결제 내역 페이지
     */
    @GetMapping("/history-page")
    public String showPaymentHistoryPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestParam(defaultValue = "0") int page,
                                         Model model) {
        try {
            Pageable pageable = PageRequest.of(page, 10);
            Page<AuctionPaymentResponse> history = auctionPaymentService.getUserPaymentHistory(
                    userDetails.getUsername(), pageable);

            AuctionPaymentService.PaymentStatistics statistics =
                    auctionPaymentService.getUserPaymentStatistics(userDetails.getUsername());

            model.addAttribute("payments", history.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", history.getTotalPages());
            model.addAttribute("statistics", statistics);

        } catch (Exception e) {
            log.error("결제 내역 페이지 로드 실패: {}", e.getMessage());
        }

        return "auction/paymentHistory";
    }
}