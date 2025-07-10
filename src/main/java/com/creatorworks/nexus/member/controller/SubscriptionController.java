package com.creatorworks.nexus.member.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.member.constant.SubscriptionStatus;
import com.creatorworks.nexus.member.dto.SubscriptionCompleteRequest;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.entity.Subscription;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.repository.SubscriptionRepository;
import com.creatorworks.nexus.member.service.SubscriptionService;
import com.creatorworks.nexus.order.service.PointService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PointService pointService;
    private final SubscriptionService subscriptionService;

    /**
     * 구독 생성 API
     * @param request 구독 생성 요청
     * @param principal 현재 로그인한 사용자
     * @return 구독 생성 결과
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @RequestBody SubscriptionCompleteRequest request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.badRequest().body(
                    SubscriptionResponse.builder()
                            .success(false)
                            .message("로그인이 필요합니다.")
                            .build()
            );
        }

        try {
            Member member = memberRepository.findByEmail(principal.getName());
            if (member == null) {
                return ResponseEntity.badRequest().body(
                        SubscriptionResponse.builder()
                                .success(false)
                                .message("회원 정보를 찾을 수 없습니다.")
                                .build()
                );
            }

            // 구독 서비스를 통한 구독 생성
            subscriptionService.createSubscription(request, member.getId());

            log.info("구독 생성 완료: 회원ID={}, 플랜={}, 작가={}, 금액={}",
                    member.getId(), request.getPlan(), request.getAuthorName(), request.getAmount());

            return ResponseEntity.ok(
                    SubscriptionResponse.builder()
                            .success(true)
                            .message("구독이 성공적으로 생성되었습니다.")
                            .plan(request.getPlan())
                            .authorName(request.getAuthorName())
                            .amount(request.getAmount())
                            .subscriptionDate(LocalDateTime.now())
                            .build()
            );

        } catch (Exception e) {
            log.error("구독 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    SubscriptionResponse.builder()
                            .success(false)
                            .message("구독 생성 중 오류가 발생했습니다: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * 구독 상태 확인 API
     */
    @PostMapping("/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkSubscription(
            @RequestBody Map<String, Object> request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            Member member = memberRepository.findByEmail(principal.getName());
            if (member == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "회원 정보를 찾을 수 없습니다."));
            }

            Long authorId = Long.valueOf(request.get("authorId").toString());
            boolean isSubscribed = subscriptionService.isSubscribed(member.getId(), authorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "isSubscribed", isSubscribed
            ));

        } catch (Exception e) {
            log.error("구독 상태 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "구독 상태 확인 중 오류가 발생했습니다."));
        }
    }

    /**
     * 구독 내역 조회 API
     */
    @GetMapping("/history")
    public String subscriptionHistory(Model model, Principal principal,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {

        if (principal == null) {
            return "redirect:/members/login";
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return "redirect:/members/login";
        }

        // 구독 내역 조회 (페이징)
        Pageable pageable = PageRequest.of(page, size, Sort.by("regTime").descending());
        Page<Subscription> subscriptionPage = subscriptionRepository.findBySubscriberOrderByRegTimeDesc(member, pageable);

        // 구독 통계
        long totalSubscriptions = subscriptionPage.getTotalElements();
        long activeSubscriptions = subscriptionRepository.findBySubscriberAndStatus(member, SubscriptionStatus.ACTIVE).size();

        model.addAttribute("subscriptions", subscriptionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", subscriptionPage.getTotalPages());
        model.addAttribute("totalSubscriptions", totalSubscriptions);
        model.addAttribute("activeSubscriptions", activeSubscriptions);
        model.addAttribute("currentBalance", pointService.getCurrentBalance(member.getId()));

        return "member/subscriptionHistory";
    }

    /**
     * 구독 해지 API
     */
    @PostMapping("/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelSubscription(
            @RequestBody Map<String, Object> request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            Member member = memberRepository.findByEmail(principal.getName());
            if (member == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "회원 정보를 찾을 수 없습니다."));
            }

            Long subscriptionId = Long.valueOf(request.get("subscriptionId").toString());
            subscriptionService.cancelSubscription(subscriptionId, member.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "구독이 성공적으로 해지되었습니다."
            ));

        } catch (Exception e) {
            log.error("구독 해지 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "구독 해지 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 구독 성공 페이지
     * @param model 모델
     * @param principal 현재 로그인한 사용자
     * @param plan 구독 플랜
     * @param amount 결제 금액
     * @param authorName 작가명
     * @param impUid 아임포트 UID
     * @param merchantUid 주문번호
     * @return 구독 성공 페이지
     */
    @GetMapping("/success")
    public String subscriptionSuccess(Model model, Principal principal,
                                      @RequestParam(required = false) String plan,
                                      @RequestParam(required = false) Long amount,
                                      @RequestParam(required = false) String authorName,
                                      @RequestParam(required = false) String impUid,
                                      @RequestParam(required = false) String merchantUid) {

        if (principal == null) {
            return "redirect:/members/login";
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return "redirect:/members/login";
        }

        // URL 파라미터에서 구독 정보 가져오기
        model.addAttribute("plan", plan != null ? plan : "월간");
        model.addAttribute("amount", amount != null ? amount : 0L);
        model.addAttribute("authorName", authorName != null ? authorName : "작가");
        model.addAttribute("impUid", impUid != null ? impUid : "N/A");
        model.addAttribute("merchantUid", merchantUid != null ? merchantUid : "N/A");
        model.addAttribute("subscriptionDate", LocalDateTime.now());
        model.addAttribute("currentBalance", pointService.getCurrentBalance(member.getId()));

        return "member/subscriptionSuccess";
    }

    /**
     * 구독 실패 페이지
     * @param model 모델
     * @param principal 현재 로그인한 사용자
     * @param errorMessage 오류 메시지
     * @param errorCode 오류 코드
     * @param plan 시도한 플랜
     * @param amount 시도한 금액
     * @param authorName 작가명
     * @return 구독 실패 페이지
     */
    @GetMapping("/fail")
    public String subscriptionFail(Model model, Principal principal,
                                   @RequestParam(required = false) String errorMessage,
                                   @RequestParam(required = false) String errorCode,
                                   @RequestParam(required = false) String plan,
                                   @RequestParam(required = false) Long amount,
                                   @RequestParam(required = false) String authorName) {

        if (principal == null) {
            return "redirect:/members/login";
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return "redirect:/members/login";
        }

        // URL 파라미터에서 오류 정보 가져오기
        model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "결제 처리 중 오류가 발생했습니다.");
        model.addAttribute("errorCode", errorCode != null ? errorCode : "UNKNOWN");
        model.addAttribute("detailedErrorMessage", "카드 잔액 부족 또는 카드 정보 오류로 인해 결제가 실패했습니다.");
        model.addAttribute("plan", plan != null ? plan : "월간");
        model.addAttribute("amount", amount != null ? amount : 0L);
        model.addAttribute("authorName", authorName != null ? authorName : "작가");
        model.addAttribute("subscriptionDate", LocalDateTime.now());
        model.addAttribute("currentBalance", pointService.getCurrentBalance(member.getId()));

        return "member/subscriptionFail";
    }

    // DTO 클래스들
    public static class SubscriptionRequest {
        private String plan;
        private Long amount;
        private String authorName;
        private Long productId;
        private String impUid;
        private String merchantUid;

        // Getters and Setters
        public String getPlan() { return plan; }
        public void setPlan(String plan) { this.plan = plan; }

        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }

        public String getAuthorName() { return authorName; }
        public void setAuthorName(String authorName) { this.authorName = authorName; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getImpUid() { return impUid; }
        public void setImpUid(String impUid) { this.impUid = impUid; }

        public String getMerchantUid() { return merchantUid; }
        public void setMerchantUid(String merchantUid) { this.merchantUid = merchantUid; }
    }

    public static class SubscriptionResponse {
        private boolean success;
        private String message;
        private String plan;
        private String authorName;
        private Long amount;
        private LocalDateTime subscriptionDate;

        // Builder pattern
        public static SubscriptionResponseBuilder builder() {
            return new SubscriptionResponseBuilder();
        }

        public static class SubscriptionResponseBuilder {
            private SubscriptionResponse response = new SubscriptionResponse();

            public SubscriptionResponseBuilder success(boolean success) {
                response.success = success;
                return this;
            }

            public SubscriptionResponseBuilder message(String message) {
                response.message = message;
                return this;
            }

            public SubscriptionResponseBuilder plan(String plan) {
                response.plan = plan;
                return this;
            }

            public SubscriptionResponseBuilder authorName(String authorName) {
                response.authorName = authorName;
                return this;
            }

            public SubscriptionResponseBuilder amount(Long amount) {
                response.amount = amount;
                return this;
            }

            public SubscriptionResponseBuilder subscriptionDate(LocalDateTime subscriptionDate) {
                response.subscriptionDate = subscriptionDate;
                return this;
            }

            public SubscriptionResponse build() {
                return response;
            }
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getPlan() { return plan; }
        public String getAuthorName() { return authorName; }
        public Long getAmount() { return amount; }
        public LocalDateTime getSubscriptionDate() { return subscriptionDate; }
    }
}