package com.creatorworks.nexus.point.service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.point.entity.Point;
import com.creatorworks.nexus.point.entity.PointHistory;
import com.creatorworks.nexus.point.repository.PointHistoryRepository;
import com.creatorworks.nexus.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {
    
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final MemberRepository memberRepository;
    
    /**
     * 회원의 포인트 잔액 조회
     */
    @Transactional(readOnly = true)
    public Integer getBalance(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        Point point = pointRepository.findByMemberId(member.getId())
                .orElseGet(() -> createInitialPoint(member.getId()));
        return point.getBalance();
    }
    
    /**
     * 포인트 사용 (차감)
     */
    public boolean usePoints(Long memberId, Integer amount, String description, Long relatedId, String relatedType) {
        Point point = pointRepository.findByMemberId(memberId)
                .orElseGet(() -> createInitialPoint(memberId));
        
        // 잔액 확인
        if (point.getBalance() < amount) {
            return false; // 잔액 부족
        }
        
        // 포인트 차감
        point.setBalance(point.getBalance() - amount);
        point.setTotalUsed(point.getTotalUsed() + amount);
        pointRepository.save(point);
        
        // 이력 저장
        PointHistory history = new PointHistory();
        history.setMember(point.getMember());
        history.setType(PointHistory.PointType.USE);
        history.setAmount(amount);
        history.setBalanceAfter(point.getBalance());
        history.setDescription(description);
        history.setRelatedId(relatedId);
        history.setRelatedType(relatedType);
        pointHistoryRepository.save(history);
        
        return true;
    }
    
    /**
     * 포인트 적립
     */
    public void earnPoints(Long memberId, Integer amount, String description, Long relatedId, String relatedType) {
        Point point = pointRepository.findByMemberId(memberId)
                .orElseGet(() -> createInitialPoint(memberId));
        
        // 포인트 적립
        point.setBalance(point.getBalance() + amount);
        point.setTotalEarned(point.getTotalEarned() + amount);
        pointRepository.save(point);
        
        // 이력 저장
        PointHistory history = new PointHistory();
        history.setMember(point.getMember());
        history.setType(PointHistory.PointType.EARN);
        history.setAmount(amount);
        history.setBalanceAfter(point.getBalance());
        history.setDescription(description);
        history.setRelatedId(relatedId);
        history.setRelatedType(relatedType);
        pointHistoryRepository.save(history);
    }
    
    /**
     * 초기 포인트 생성
     */
    private Point createInitialPoint(Long memberId) {
        Point point = new Point();
        Member member = new Member();
        member.setId(memberId);
        point.setMember(member);
        point.setBalance(0);
        point.setTotalEarned(0);
        point.setTotalUsed(0);
        return pointRepository.save(point);
    }
} 