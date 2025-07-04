package com.creatorworks.nexus.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> applyForSeller(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
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
} 