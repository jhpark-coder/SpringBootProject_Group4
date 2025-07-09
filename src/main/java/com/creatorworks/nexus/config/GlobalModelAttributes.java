package com.creatorworks.nexus.config;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.service.PointService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final PointService pointService;
    private final MemberRepository memberRepository;

    @ModelAttribute("currentPoint")
    public Long getCurrentPoint(Principal principal) {
        if (principal == null) {
            return 0L;
        }
        
        try {
            Member member = memberRepository.findByEmail(principal.getName());
            if (member != null) {
                return pointService.getCurrentBalance(member.getId());
            }
        } catch (Exception e) {
            // 로그인은 되어 있지만 포인트 조회 중 오류가 발생한 경우
            return 0L;
        }
        
        return 0L;
    }
} 