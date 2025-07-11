package com.creatorworks.nexus.order.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.creatorworks.nexus.order.dto.RefundRequest;
import com.creatorworks.nexus.order.dto.RefundResponse;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Payment;
import com.creatorworks.nexus.order.entity.Refund;
import com.creatorworks.nexus.order.entity.Refund.RefundStatus;
import com.creatorworks.nexus.order.service.RefundService;
import com.creatorworks.nexus.order.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final MemberRepository memberRepository;
    private final OrderService orderService;

    /**
     * 환불 요청 페이지
     */
    @GetMapping("/request/{orderId}")
    public String refundRequestForm(@PathVariable Long orderId, Model model) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Member member = memberRepository.findByEmail(username);
            if (member == null) {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }

            // 주문 정보 조회
            Order order = orderService.findById(orderId).orElse(null);
            if (order == null || !order.getBuyer().getId().equals(member.getId())) {
                return "redirect:/error?message=주문 정보를 찾을 수 없습니다.";
            }

            model.addAttribute("orderId", orderId);
            model.addAttribute("member", member);
            model.addAttribute("order", order);
            
            // 포인트 구매 상품인지 확인
            boolean isPointPurchase = order.getOrderType() == Order.OrderType.PRODUCT_PURCHASE && 
                                    order.getPayment() != null && 
                                    order.getPayment().getPaymentType() == Payment.PaymentType.POINT;
            model.addAttribute("isPointPurchase", isPointPurchase);
            
            return "order/refund-request";
        } catch (Exception e) {
            log.error("환불 요청 페이지 로드 오류: orderId={}, 오류={}", orderId, e.getMessage(), e);
            return "redirect:/error";
        }
    }

    /**
     * 환불 요청 처리
     */
    @PostMapping("/request")
    @ResponseBody
    public ResponseEntity<RefundResponse> requestRefund(@RequestBody RefundRequest request) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Member member = memberRepository.findByEmail(username);
            if (member == null) {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }

            // 포인트 구매 상품의 경우 자동으로 구매 금액 설정
            if (request.getOrderId() != null) {
                Order order = orderService.findById(request.getOrderId()).orElse(null);
                if (order != null && 
                    order.getOrderType() == Order.OrderType.PRODUCT_PURCHASE && 
                    order.getPayment() != null && 
                    order.getPayment().getPaymentType() == Payment.PaymentType.POINT) {
                    
                    // 포인트 구매 상품의 경우 구매 금액으로 자동 설정
                    request.setAmount(order.getTotalAmount());
                    log.info("포인트 구매 상품 환불 요청: 주문ID={}, 자동 설정 금액={}", 
                            request.getOrderId(), request.getAmount());
                }
            }

            // 환불 요청 처리
            RefundResponse response = refundService.createRefundRequest(request, member.getId());
            
            if (response.isSuccess()) {
                log.info("환불 요청 성공: 사용자={}, 주문={}, 금액={}", 
                        member.getEmail(), request.getOrderId(), request.getAmount());
                return ResponseEntity.ok(response);
            } else {
                log.warn("환불 요청 실패: 사용자={}, 주문={}, 사유={}", 
                        member.getEmail(), request.getOrderId(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("환불 요청 처리 오류: 오류={}", e.getMessage(), e);
            RefundResponse errorResponse = RefundResponse.builder()
                    .success(false)
                    .message("환불 요청 처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 내 환불 내역 조회
     */
    @GetMapping("/my-refunds")
    public String getMyRefunds(Model model, 
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Member member = memberRepository.findByEmail(username);
            if (member == null) {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }

            // 내 환불 내역 조회
            List<Refund> refunds = refundService.getMyRefunds(member.getId(), page, size);
            long totalRefunds = refundService.getMyRefundsCount(member.getId());
            
            // 통계 데이터 계산
            long pendingCount = refunds.stream()
                    .filter(r -> r.getRefundStatus() == RefundStatus.PENDING)
                    .count();
            long completedCount = refunds.stream()
                    .filter(r -> r.getRefundStatus() == RefundStatus.COMPLETED)
                    .count();
            long failedCount = refunds.stream()
                    .filter(r -> r.getRefundStatus() == RefundStatus.FAILED)
                    .count();
            
            model.addAttribute("refunds", refunds);
            model.addAttribute("totalRefunds", totalRefunds);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("member", member);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("failedCount", failedCount);
            
            return "order/my-refunds";
        } catch (Exception e) {
            log.error("내 환불 내역 조회 오류: 오류={}", e.getMessage(), e);
            return "redirect:/error";
        }
    }

    /**
     * 환불 상세 조회
     */
    @GetMapping("/detail/{refundId}")
    public String getRefundDetail(@PathVariable Long refundId, Model model) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Member member = memberRepository.findByEmail(username);
            if (member == null) {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }

            // 환불 상세 정보 조회
            Refund refund = refundService.getRefundDetail(refundId, member.getId());
            
            if (refund == null) {
                return "redirect:/error?message=환불 정보를 찾을 수 없습니다.";
            }
            
            model.addAttribute("refund", refund);
            model.addAttribute("member", member);
            
            return "order/refund-detail";
        } catch (Exception e) {
            log.error("환불 상세 조회 오류: refundId={}, 오류={}", refundId, e.getMessage(), e);
            return "redirect:/error";
        }
    }

    /**
     * 환불 요청 취소
     */
    @PostMapping("/cancel/{refundId}")
    @ResponseBody
    public ResponseEntity<RefundResponse> cancelRefund(@PathVariable Long refundId) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Member member = memberRepository.findByEmail(username);
            if (member == null) {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }

            // 환불 요청 취소
            RefundResponse response = refundService.cancelRefundRequest(refundId, member.getId());
            
            if (response.isSuccess()) {
                log.info("환불 요청 취소 성공: 사용자={}, 환불={}", member.getEmail(), refundId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("환불 요청 취소 실패: 사용자={}, 환불={}, 사유={}", 
                        member.getEmail(), refundId, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("환불 요청 취소 처리 오류: refundId={}, 오류={}", refundId, e.getMessage(), e);
            RefundResponse errorResponse = RefundResponse.builder()
                    .success(false)
                    .message("환불 요청 취소 처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 환불 상태 확인 API
     */
    @GetMapping("/status/{refundId}")
    @ResponseBody
    public ResponseEntity<RefundResponse> getRefundStatus(@PathVariable Long refundId) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Member member = memberRepository.findByEmail(username);
            if (member == null) {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }

            // 환불 상태 조회
            Refund refund = refundService.getRefundDetail(refundId, member.getId());
            
            if (refund == null) {
                RefundResponse errorResponse = RefundResponse.builder()
                        .success(false)
                        .message("환불 정보를 찾을 수 없습니다.")
                        .build();
                return ResponseEntity.notFound().build();
            }
            
            RefundResponse response = RefundResponse.builder()
                    .success(true)
                    .refundId(refund.getId())
                    .status(refund.getStatus().toString())
                    .amount(refund.getAmount())
                    .reason(refund.getReason())
                    .requestDate(refund.getRequestDate())
                    .processedDate(refund.getProcessedDate())
                    .message("환불 상태 조회 성공")
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("환불 상태 조회 오류: refundId={}, 오류={}", refundId, e.getMessage(), e);
            RefundResponse errorResponse = RefundResponse.builder()
                    .success(false)
                    .message("환불 상태 조회 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 