package com.creatorworks.nexus.member.service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.entity.MemberFollow;
import com.creatorworks.nexus.member.repository.MemberFollowRepository;
import com.creatorworks.nexus.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberFollowService {

    private final MemberFollowRepository memberFollowRepository;
    private final MemberRepository memberRepository;

    /**
     * 팔로우/언팔로우 토글
     * @param followerId 팔로우하는 사용자 ID
     * @param followingId 팔로우받는 사용자 ID
     * @return 팔로우 상태 (true: 팔로우됨, false: 언팔로우됨)
     */
    @Transactional
    public boolean toggleFollow(Long followerId, Long followingId) {
        // 자기 자신을 팔로우할 수 없음
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우하는 사용자를 찾을 수 없습니다."));
        
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우받는 사용자를 찾을 수 없습니다."));

        // 기존 팔로우 관계 확인
        Optional<MemberFollow> existingFollow = memberFollowRepository.findByFollowerAndFollowing(follower, following);

        if (existingFollow.isPresent()) {
            // 팔로우 관계가 있으면 삭제 (언팔로우)
            memberFollowRepository.delete(existingFollow.get());
            return false;
        } else {
            // 팔로우 관계가 없으면 생성 (팔로우)
            MemberFollow newFollow = new MemberFollow(follower, following);
            memberFollowRepository.save(newFollow);
            return true;
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