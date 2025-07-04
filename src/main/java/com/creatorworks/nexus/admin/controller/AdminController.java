package com.creatorworks.nexus.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.admin.service.SellerRequestService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/admin")
@Controller
@RequiredArgsConstructor
public class AdminController {

    private final SellerRequestService sellerRequestService;

    @GetMapping("/seller-management")
    public String sellerManagement(Model model) {
        model.addAttribute("sellerRequests", sellerRequestService.getPendingRequests());
        return "admin/seller-management";
    }

    @PostMapping("/seller-request/{requestId}/approve")
    @ResponseBody
    public ResponseEntity<String> approveSellerRequest(@PathVariable Long requestId) {
        try {
            sellerRequestService.approveRequest(requestId);
            return ResponseEntity.ok("승인 처리되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/seller-request/{requestId}/reject")
    @ResponseBody
    public ResponseEntity<String> rejectSellerRequest(@PathVariable Long requestId, 
                                                     @RequestParam(required = false) String reason) {
        try {
            sellerRequestService.rejectRequest(requestId, reason != null ? reason : "관리자에 의해 거절되었습니다.");
            return ResponseEntity.ok("거절 처리되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 