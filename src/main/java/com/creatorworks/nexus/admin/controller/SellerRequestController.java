package com.creatorworks.nexus.admin.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatorworks.nexus.admin.entity.SellerRequest;
import com.creatorworks.nexus.admin.service.SellerRequestService;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/seller-request")
@RequiredArgsConstructor
public class SellerRequestController {

    private final SellerRequestService sellerRequestService;
    private final MemberRepository memberRepository;

    @PostMapping("/apply")
    public ResponseEntity<String> applyForSeller(@AuthenticationPrincipal Object principal) {
        try {
            String email = getEmailFromPrincipal(principal);
            Member member = memberRepository.findByEmail(email);
            if (member == null) {
                throw new IllegalArgumentException("회원을 찾을 수 없습니다.");
            }

            sellerRequestService.createSellerRequest(member);
            return ResponseEntity.ok("작가 등록이 신청되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("작가 신청 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Object> getSellerRequestStatus(@AuthenticationPrincipal Object principal) {
        try {
            String email = getEmailFromPrincipal(principal);
            Member member = memberRepository.findByEmail(email);
            if (member == null) {
                return ResponseEntity.badRequest().body("회원을 찾을 수 없습니다.");
            }

            SellerRequest sellerRequest = sellerRequestService.getLatestSellerRequest(member.getId());
            
            if (sellerRequest == null) {
                return ResponseEntity.ok(Map.of("status", "NONE", "message", "신청 내역이 없습니다."));
            }

            return ResponseEntity.ok(Map.of(
                "status", sellerRequest.getStatus().name(),
                "message", sellerRequest.getStatus().getDescription(),
                "requestDate", sellerRequest.getRegTime(),
                "reason", sellerRequest.getReason()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("상태 확인 중 오류가 발생했습니다.");
        }
    }

    private String getEmailFromPrincipal(Object principal) {
        if (principal == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            return ((org.springframework.security.oauth2.core.user.OAuth2User) principal).getName();
        } else {
            return principal.toString();
        }
    }
} 