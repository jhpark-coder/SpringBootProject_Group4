package com.creatorworks.nexus.product.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.dto.PointChargeRequest;
import com.creatorworks.nexus.product.dto.PointPurchaseRequest;
import com.creatorworks.nexus.product.dto.PointResponse;
import com.creatorworks.nexus.product.entity.Point;
import com.creatorworks.nexus.product.service.PointService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Slf4j
public class PointController {

    private final PointService pointService;
    private final MemberRepository memberRepository;

    /**
     * 포인트 충전 API
     * @param request 포인트 충전 요청
     * @param principal 현재 로그인한 사용자
     * @return 포인트 충전 결과
     */
    @PostMapping("/charge")
    @ResponseBody
    public ResponseEntity<PointResponse> chargePoint(@RequestBody PointChargeRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(
                PointResponse.builder()
                    .success(false)
                    .message("로그인이 필요합니다.")
                    .build()
            );
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return ResponseEntity.badRequest().body(
                PointResponse.builder()
                    .success(false)
                    .message("회원 정보를 찾을 수 없습니다.")
                    .build()
            );
        }

        try {
            PointResponse response = pointService.chargePoint(member.getId(), request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("포인트 충전 실패: 회원ID={}, 오류={}", member.getId(), e.getMessage());
            return ResponseEntity.badRequest().body(
                PointResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("포인트 충전 중 예상치 못한 오류: 회원ID={}, 오류={}", member.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                PointResponse.builder()
                    .success(false)
                    .message("포인트 충전 중 오류가 발생했습니다.")
                    .build()
            );
        }
    }

    /**
     * 포인트로 상품 구매 API
     * @param productId 상품 ID
     * @param request 포인트 구매 요청
     * @param principal 현재 로그인한 사용자
     * @return 포인트 구매 결과
     */
    @PostMapping("/products/{productId}/purchase")
    @ResponseBody
    public ResponseEntity<PointResponse> purchaseWithPoint(
            @PathVariable Long productId,
            @RequestBody PointPurchaseRequest request,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.badRequest().body(
                PointResponse.builder()
                    .success(false)
                    .message("로그인이 필요합니다.")
                    .build()
            );
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return ResponseEntity.badRequest().body(
                PointResponse.builder()
                    .success(false)
                    .message("회원 정보를 찾을 수 없습니다.")
                    .build()
            );
        }

        // 요청 데이터 설정
        request.setProductId(productId);

        try {
            PointResponse response = pointService.purchaseWithPoint(member.getId(), request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("포인트 구매 실패: 회원ID={}, 상품ID={}, 오류={}", member.getId(), productId, e.getMessage());
            return ResponseEntity.badRequest().body(
                PointResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("포인트 구매 중 예상치 못한 오류: 회원ID={}, 상품ID={}, 오류={}", member.getId(), productId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                PointResponse.builder()
                    .success(false)
                    .message("포인트 구매 중 오류가 발생했습니다.")
                    .build()
            );
        }
    }

    /**
     * 현재 포인트 잔액 조회 API
     * @param principal 현재 로그인한 사용자
     * @return 포인트 잔액
     */
    @GetMapping("/balance")
    @ResponseBody
    public ResponseEntity<PointResponse> getCurrentBalance(Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(
                PointResponse.builder()
                    .success(false)
                    .message("로그인이 필요합니다.")
                    .build()
            );
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return ResponseEntity.badRequest().body(
                PointResponse.builder()
                    .success(false)
                    .message("회원 정보를 찾을 수 없습니다.")
                    .build()
            );
        }

        Long balance = pointService.getCurrentBalance(member.getId());
        
        return ResponseEntity.ok(
            PointResponse.builder()
                .success(true)
                .currentBalance(balance)
                .message("포인트 잔액 조회 완료")
                .build()
        );
    }

    /**
     * 포인트 내역 조회 API
     * @param principal 현재 로그인한 사용자
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
     */
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<Page<Point>> getPointHistory(
            Principal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        
        if (principal == null) {
            return ResponseEntity.badRequest().build();
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return ResponseEntity.badRequest().build();
        }

        Page<Point> pointHistory = pointService.getPointHistory(member.getId(), pageable);
        return ResponseEntity.ok(pointHistory);
    }

    /**
     * 포인트 내역 페이지 (뷰)
     * @param principal 현재 로그인한 사용자
     * @param model 모델
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
     */
    @GetMapping("/history/page")
    public String getPointHistoryPage(
            Principal principal,
            Model model,
            @PageableDefault(size = 20) Pageable pageable) {
        
        if (principal == null) {
            return "redirect:/members/login";
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return "redirect:/members/login";
        }

        Long currentBalance = pointService.getCurrentBalance(member.getId());
        Page<Point> pointHistory = pointService.getPointHistory(member.getId(), pageable);

        model.addAttribute("currentBalance", currentBalance);
        model.addAttribute("pointHistory", pointHistory);
        model.addAttribute("member", member);

        return "member/pointHistory";
    }

    /**
     * 포인트 충전 성공 페이지
     * @param model 모델
     * @param principal 현재 로그인한 사용자
     * @param amount 충전 금액
     * @param paymentAmount 결제 금액
     * @param paymentMethod 결제 방법
     * @param merchantUid 주문번호
     * @param impUid 아임포트 UID
     * @return 포인트 충전 성공 페이지
     */
    @GetMapping("/charge/success")
    public String chargeSuccess(Model model, Principal principal,
                               @RequestParam(required = false) Long amount,
                               @RequestParam(required = false) Long paymentAmount,
                               @RequestParam(required = false) String paymentMethod,
                               @RequestParam(required = false) String merchantUid,
                               @RequestParam(required = false) String impUid) {
        if (principal == null) {
            return "redirect:/members/login";
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return "redirect:/members/login";
        }

        // URL 파라미터에서 결제 정보 가져오기
        model.addAttribute("amount", amount != null ? amount : 0L);
        model.addAttribute("paymentAmount", paymentAmount != null ? paymentAmount : 0L);
        model.addAttribute("paymentMethod", paymentMethod != null ? paymentMethod : "신용카드");
        model.addAttribute("transactionDate", LocalDateTime.now());
        model.addAttribute("merchantUid", merchantUid != null ? merchantUid : "N/A");
        model.addAttribute("currentBalance", pointService.getCurrentBalance(member.getId()));

        return "member/pointSuccess";
    }

    /**
     * 포인트 충전 실패 페이지
     * @param model 모델
     * @param principal 현재 로그인한 사용자
     * @param errorMessage 오류 메시지
     * @param errorCode 오류 코드
     * @param amount 시도한 금액
     * @param paymentMethod 결제 방법
     * @return 포인트 충전 실패 페이지
     */
    @GetMapping("/charge/fail")
    public String chargeFail(Model model, Principal principal,
                            @RequestParam(required = false) String errorMessage,
                            @RequestParam(required = false) String errorCode,
                            @RequestParam(required = false) Long amount,
                            @RequestParam(required = false) String paymentMethod) {
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
        model.addAttribute("amount", amount != null ? amount : 0L);
        model.addAttribute("paymentMethod", paymentMethod != null ? paymentMethod : "신용카드");
        model.addAttribute("transactionDate", LocalDateTime.now());
        model.addAttribute("currentBalance", pointService.getCurrentBalance(member.getId()));

        return "member/pointFail";
    }
} 