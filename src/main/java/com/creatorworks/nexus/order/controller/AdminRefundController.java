package com.creatorworks.nexus.order.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.creatorworks.nexus.order.dto.RefundResponse;
import com.creatorworks.nexus.order.dto.RefundStatisticsDto;
import com.creatorworks.nexus.order.entity.Refund;
import com.creatorworks.nexus.order.entity.Refund.RefundStatus;
import com.creatorworks.nexus.order.entity.Refund.RefundType;
import com.creatorworks.nexus.order.service.RefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/refund")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    /**
     * 환불 관리 메인 페이지
     */
    @GetMapping
    public String refundManagement(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 @RequestParam(required = false) RefundStatus status,
                                 @RequestParam(required = false) RefundType type) {
        try {
            log.info("환불 관리 페이지 로드 시작");
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Refund> refunds;
            
            log.info("환불 목록 조회 시작");
            if (status != null && type != null) {
                refunds = refundService.getRefundsByStatusAndType(status, type, pageable);
            } else if (status != null) {
                refunds = refundService.getRefundsByStatus(status, pageable);
            } else if (type != null) {
                refunds = refundService.getRefundsByType(type, pageable);
            } else {
                refunds = refundService.getRecentRefunds(pageable);
            }
            log.info("환불 목록 조회 완료: {}개", refunds.getTotalElements());
            
            // 환불 통계 조회
            log.info("환불 통계 조회 시작");
            RefundStatisticsDto statistics = refundService.getRefundStatistics();
            log.info("환불 통계 조회 완료: {}", statistics);
            
            model.addAttribute("refunds", refunds);
            model.addAttribute("statistics", statistics);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedType", type);
            model.addAttribute("statuses", RefundStatus.values());
            model.addAttribute("types", RefundType.values());
            
            log.info("환불 관리 페이지 로드 완료");
            return "admin/refund-management";
        } catch (Exception e) {
            log.error("환불 관리 페이지 로드 오류: 오류={}", e.getMessage(), e);
            return "redirect:/error";
        }
    }

    /**
     * 환불 상세 조회 (관리자용)
     */
    @GetMapping("/detail/{refundId}")
    public String getRefundDetail(@PathVariable Long refundId, Model model) {
        try {
            Refund refund = refundService.getRefundById(refundId);
            
            if (refund == null) {
                return "redirect:/error?message=환불 정보를 찾을 수 없습니다.";
            }
            
            model.addAttribute("refund", refund);
            model.addAttribute("statuses", RefundStatus.values());
            
            return "admin/refund-detail";
        } catch (Exception e) {
            log.error("환불 상세 조회 오류: refundId={}, 오류={}", refundId, e.getMessage(), e);
            return "redirect:/error";
        }
    }

    /**
     * 환불 처리
     */
    @PostMapping("/process/{refundId}")
    public String processRefund(@PathVariable Long refundId, RedirectAttributes redirectAttributes) {
        try {
            Refund refund = refundService.processRefund(refundId);
            
            log.info("환불 처리 완료: 환불ID={}, 상태={}", refundId, refund.getRefundStatus());
            redirectAttributes.addFlashAttribute("successMessage", "환불 처리가 완료되었습니다.");
            
        } catch (Exception e) {
            log.error("환불 처리 오류: refundId={}, 오류={}", refundId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "환불 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/refund";
    }

    /**
     * 환불 재처리
     */
    @PostMapping("/retry/{refundId}")
    public String retryRefund(@PathVariable Long refundId, RedirectAttributes redirectAttributes) {
        try {
            Refund refund = refundService.retryRefund(refundId);
            
            log.info("환불 재처리 완료: 환불ID={}, 상태={}", refundId, refund.getRefundStatus());
            redirectAttributes.addFlashAttribute("successMessage", "환불 재처리가 완료되었습니다.");
            
        } catch (Exception e) {
            log.error("환불 재처리 오류: refundId={}, 오류={}", refundId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "환불 재처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/refund";
    }

    /**
     * 환불 요청 취소
     */
    @PostMapping("/cancel/{refundId}")
    public String cancelRefund(@PathVariable Long refundId, RedirectAttributes redirectAttributes) {
        try {
            Refund refund = refundService.cancelRefund(refundId);
            
            log.info("환불 요청 취소 완료: 환불ID={}, 상태={}", refundId, refund.getRefundStatus());
            redirectAttributes.addFlashAttribute("successMessage", "환불 요청이 취소되었습니다.");
            
        } catch (Exception e) {
            log.error("환불 요청 취소 오류: refundId={}, 오류={}", refundId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "환불 요청 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/refund";
    }

    /**
     * 관리자 코멘트 추가
     */
    @PostMapping("/comment/{refundId}")
    @ResponseBody
    public ResponseEntity<RefundResponse> addComment(@PathVariable Long refundId,
                                                   @RequestBody String comment) {
        try {
            Refund refund = refundService.addAdminComment(refundId, comment);
            
            RefundResponse response = RefundResponse.builder()
                    .success(true)
                    .refundId(refund.getId())
                    .message("관리자 코멘트가 추가되었습니다.")
                    .build();
            
            log.info("관리자 코멘트 추가: 환불ID={}, 코멘트={}", refundId, comment);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("관리자 코멘트 추가 오류: refundId={}, 오류={}", refundId, e.getMessage(), e);
            RefundResponse errorResponse = RefundResponse.builder()
                    .success(false)
                    .message("관리자 코멘트 추가 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 환불 통계 API
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<RefundStatisticsDto> getStatistics() {
        try {
            RefundStatisticsDto statistics = refundService.getRefundStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("환불 통계 조회 오류: 오류={}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 환불 상태별 목록 조회 API
     */
    @GetMapping("/status/{status}")
    @ResponseBody
    public ResponseEntity<Page<Refund>> getRefundsByStatus(@PathVariable RefundStatus status,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Refund> refunds = refundService.getRefundsByStatus(status, pageable);
            return ResponseEntity.ok(refunds);
        } catch (Exception e) {
            log.error("환불 상태별 목록 조회 오류: status={}, 오류={}", status, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 환불 타입별 목록 조회 API
     */
    @GetMapping("/type/{type}")
    @ResponseBody
    public ResponseEntity<Page<Refund>> getRefundsByType(@PathVariable RefundType type,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Refund> refunds = refundService.getRefundsByType(type, pageable);
            return ResponseEntity.ok(refunds);
        } catch (Exception e) {
            log.error("환불 타입별 목록 조회 오류: type={}, 오류={}", type, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 회원별 환불 내역 조회
     */
    @GetMapping("/member/{memberId}")
    public String getMemberRefunds(@PathVariable Long memberId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Refund> refunds = refundService.getRefundHistory(memberId, pageable);
            
            model.addAttribute("refunds", refunds);
            model.addAttribute("memberId", memberId);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            
            return "admin/member-refunds";
        } catch (Exception e) {
            log.error("회원별 환불 내역 조회 오류: memberId={}, 오류={}", memberId, e.getMessage(), e);
            return "redirect:/error";
        }
    }
} 