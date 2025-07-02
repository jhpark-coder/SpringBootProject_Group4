package com.creatorworks.nexus.point.service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointChargeService {
    
    private final PointService pointService;
    private final MemberRepository memberRepository;
    
    /**
     * 포인트 충전
     */
    public void chargePoints(String email, Integer amount, String paymentMethod) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // 포인트 적립 (충전)
        pointService.earnPoints(
                member.getId(),
                amount,
                "포인트 충전 (" + paymentMethod + ")",
                null,
                "CHARGE"
        );
    }
    
    /**
     * 포인트 충전 이력 조회
     */
    @Transactional(readOnly = true)
    public Integer getChargeHistory(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        return pointService.getBalance(email);
    }
} 