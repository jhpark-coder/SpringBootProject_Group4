package com.creatorworks.nexus.member.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.dto.FollowingDto;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.entity.MemberFollow;
import com.creatorworks.nexus.member.repository.MemberFollowRepository;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.notification.dto.FollowNotificationRequest;
import com.creatorworks.nexus.notification.entity.NotificationCategory;
import com.creatorworks.nexus.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberFollowService {

    private final MemberFollowRepository memberFollowRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    /**
     * 팔로우/언팔로우 토글
     * @param followerId 팔로우하는 사용자 ID
     * @param followingId 팔로우받는 사용자 ID
     * @return 팔로우 상태 (true: 팔로우됨, false: 언팔로우됨)
     */
    @Transactional
    public boolean toggleFollow(Long followerId, Long followingId) {
        // 팔로우 토글 처리 시작
        
        // 자기 자신을 팔로우할 수 없음
        if (followerId.equals(followingId)) {
            // 자기 자신을 팔로우하려고 시도
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        try {
            Member follower = memberRepository.findById(followerId)
                    .orElseThrow(() -> new IllegalArgumentException("팔로우하는 사용자를 찾을 수 없습니다."));
            // 팔로워 정보 확인
            
            Member following = memberRepository.findById(followingId)
                    .orElseThrow(() -> new IllegalArgumentException("팔로우받는 사용자를 찾을 수 없습니다."));
            // 팔로잉 정보 확인

            // 기존 팔로우 관계 확인
            Optional<MemberFollow> existingFollow = memberFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId);
            // 기존 팔로우 관계 확인

            if (existingFollow.isPresent()) {
                // 팔로우 관계가 있으면 삭제 (언팔로우)
                        // 언팔로우 처리
        memberFollowRepository.delete(existingFollow.get());
                return false;
            } else {
                // 팔로우 관계가 없으면 생성 (팔로우)
                // 팔로우 처리
                MemberFollow newFollow = new MemberFollow(follower, following);
                memberFollowRepository.save(newFollow);
                
                // 팔로우 알림 생성 및 전송 (중복 체크 포함)
                // 팔로우 알림 생성 시작
                FollowNotificationRequest followNotificationRequest = new FollowNotificationRequest();
                followNotificationRequest.setTargetUserId(followingId); // 알림 받을 사람(팔로우 당한 사람)
                followNotificationRequest.setSenderUserId(followerId);   // 알림 보낸 사람(팔로우 건 사람)
                followNotificationRequest.setMessage(follower.getName() + "님이 회원님을 팔로우했습니다!");
                followNotificationRequest.setType("follow");
                followNotificationRequest.setCategory(NotificationCategory.SOCIAL); // 카테고리 설정

                // 팔로우 알림은 별도의 링크를 연결하지 않도록 null로 설정
                var savedNotification = notificationService.saveNotification(followNotificationRequest, null);
                
                if (savedNotification != null) {
                    // 새로운 팔로우 알림인 경우에만 WebSocket 전송
                    // 알림 DB 저장 완료
                    notificationService.sendNotification(followNotificationRequest);
                } else {
                    // 중복 팔로우 알림인 경우
                    // 알림 중복 방지
                }

                return true;
            }
        } catch (Exception e) {
            System.err.println("MemberFollowService.toggleFollow에서 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 팔로우 상태 확인
     * @param followerId 팔로우하는 사용자 ID
     * @param followingId 팔로우받는 사용자 ID
     * @return 팔로우 상태
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        return memberFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    /**
     * 사용자의 팔로워 수 조회
     * @param memberId 사용자 ID
     * @return 팔로워 수
     */
    public long getFollowerCount(Long memberId) {
        return memberFollowRepository.countFollowersByMemberId(memberId);
    }

    /**
     * 사용자의 팔로잉 수 조회
     * @param memberId 사용자 ID
     * @return 팔로잉 수
     */
    public long getFollowingCount(Long memberId) {
        return memberFollowRepository.countFollowingsByMemberId(memberId);
    }

    /**
     * 사용자의 팔로워 목록 조회
     * @param memberId 사용자 ID
     * @return 팔로워 목록
     */
    public List<Member> getFollowers(Long memberId) {
        return memberFollowRepository.findFollowersByMemberId(memberId);
    }

    /**
     * 사용자가 팔로우하는 사람 목록 조회
     * @param memberId 사용자 ID
     * @return 팔로잉 목록
     */
    public List<Member> getFollowings(Long memberId) {
        return memberFollowRepository.findFollowingsByMemberId(memberId);
    }

    /**
     * 사용자가 팔로우하는 사람 목록을 팔로우 날짜와 함께 조회
     * @param memberId 사용자 ID
     * @return 팔로잉 목록 (팔로우 날짜 포함)
     */
    public List<FollowingDto> getFollowingsWithDate(Long memberId) {
        List<MemberFollow> memberFollows = memberFollowRepository.findFollowingsWithDateByMemberId(memberId);
        return memberFollows.stream()
                .map(mf -> new FollowingDto(
                        mf.getFollowing().getId(),
                        mf.getFollowing().getName(),
                        mf.getFollowing().getEmail(),
                        mf.getRegTime()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 팔로우 통계 정보 조회
     * @param memberId 사용자 ID
     * @return 팔로우 통계 정보
     */
    public Map<String, Object> getFollowStats(Long memberId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("followerCount", getFollowerCount(memberId));
        stats.put("followingCount", getFollowingCount(memberId));
        return stats;
    }

    /**
     * 팔로우 관계 상세 정보 조회
     * @param followerId 팔로우하는 사용자 ID
     * @param followingId 팔로우받는 사용자 ID
     * @return 팔로우 관계 정보
     */
    public Map<String, Object> getFollowInfo(Long followerId, Long followingId) {
        Map<String, Object> info = new HashMap<>();
        info.put("isFollowing", isFollowing(followerId, followingId));
        info.put("followerCount", getFollowerCount(followingId));
        info.put("followingCount", getFollowingCount(followingId));
        return info;
    }
} 