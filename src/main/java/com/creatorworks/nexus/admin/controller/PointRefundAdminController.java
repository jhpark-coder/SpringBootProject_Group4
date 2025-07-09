package com.creatorworks.nexus.admin.controller;

import java.security.Principal;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.dto.PointResponse;
import com.creatorworks.nexus.product.entity.PointRefund;
import com.creatorworks.nexus.product.entity.PointRefund.RefundStatus;
import com.creatorworks.nexus.product.repository.PointRefundRepository;
import com.creatorworks.nexus.product.service.PointService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/point-refunds")
@RequiredArgsConstructor
@Slf4j
public class PointRefundAdminController {

    private final PointService pointService;
    private final PointRefundRepository pointRefundRepository;
    private final MemberRepository memberRepository;

    /**
     * 환불 요청 목록 페이지
     * @param model 모델
     * @param principal 현재 로그인한 사용자
     * @param status 환불 상태 필터
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 환불 요청 목록 페이지
     */
    @GetMapping
    public String refundList(Model model, Principal principal,
                           @RequestParam(required = false) String status,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size) {
        
        if (principal == null) {
            return "redirect:/members/login";
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null || !member.getRole().name().equals("ADMIN")) {
            return "redirect:/members/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("regTime").descending());
        Page<PointRefund> refundPage;

        if (status != null && !status.isEmpty()) {
            try {
                RefundStatus refundStatus = RefundStatus.valueOf(status.toUpperCase());
                refundPage = pointRefundRepository.findByStatusOrderByRegTimeDesc(refundStatus, pageable);
            } catch (IllegalArgumentException e) {
                refundPage = pointRefundRepository.findAll(pageable);
            }
        } else {
            refundPage = pointRefundRepository.findAll(pageable);
        }

        // 통계 정보
        long totalRefunds = pointRefundRepository.count();
        long pendingRefunds = pointRefundRepository.countByStatus(RefundStatus.PENDING);
        long approvedRefunds = pointRefundRepository.countByStatus(RefundStatus.APPROVED);
        long completedRefunds = pointRefundRepository.countByStatus(RefundStatus.COMPLETED);

        model.addAttribute("refunds", refundPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", refundPage.getTotalPages());
        model.addAttribute("totalRefunds", totalRefunds);
        model.addAttribute("pendingRefunds", pendingRefunds);
        model.addAttribute("approvedRefunds", approvedRefunds);
        model.addAttribute("completedRefunds", completedRefunds);
        model.addAttribute("currentStatus", status);

        return "admin/pointRefundList";
    }

    /**
     * 환불 요청 상세 페이지
     * @param model 모델
     * @param principal 현재 로그인한 사용자
     * @param refundId 환불 요청 ID
     * @return 환불 요청 상세 페이지
     */
    @GetMapping("/{refundId}")
    public String refundDetail(Model model, Principal principal, @PathVariable Long refundId) {
        
        if (principal == null) {
            return "redirect:/members/login";
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null || !member.getRole().name().equals("ADMIN")) {
            return "redirect:/members/login";
        }

        PointRefund refund = pointRefundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 요청을 찾을 수 없습니다."));

        model.addAttribute("refund", refund);
        model.addAttribute("member", member);

        return "admin/pointRefundDetail";
    }

    /**
     * 환불 요청 처리 API
     * @param refundId 환불 요청 ID
     * @param request 처리 요청 (approved: 승인여부, comment: 관리자 코멘트)
     * @param principal 현재 로그인한 사용자
     * @return 처리 결과
     */
    @PostMapping("/{refundId}/process")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable Long refundId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null || !member.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "관리자 권한이 필요합니다."));
        }

        try {
            Boolean approved = (Boolean) request.get("approved");
            String comment = (String) request.get("comment");

            if (approved == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "승인 여부를 지정해주세요."));
            }

            PointResponse response = pointService.processRefund(refundId, approved, comment);

            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", response.getMessage()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", response.getMessage()
                ));
            }
        } catch (IllegalArgumentException e) {
            log.error("환불 처리 실패: 환불ID={}, 오류={}", refundId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("환불 처리 중 예상치 못한 오류: 환불ID={}, 오류={}", refundId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "환불 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 환불 완료 처리 API
     * @param refundId 환불 요청 ID
     * @param principal 현재 로그인한 사용자
     * @return 처리 결과
     */
    @PostMapping("/{refundId}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeRefund(
            @PathVariable Long refundId,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null || !member.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "관리자 권한이 필요합니다."));
        }

        try {
            PointResponse response = pointService.completeRefund(refundId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", response.getMessage()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", response.getMessage()
                ));
            }
        } catch (IllegalArgumentException e) {
            log.error("환불 완료 처리 실패: 환불ID={}, 오류={}", refundId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("환불 완료 처리 중 예상치 못한 오류: 환불ID={}, 오류={}", refundId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "환불 완료 처리 중 오류가 발생했습니다."));
        }
    }
} 