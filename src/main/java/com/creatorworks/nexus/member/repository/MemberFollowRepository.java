package com.creatorworks.nexus.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.entity.MemberFollow;

@Repository
public interface MemberFollowRepository extends JpaRepository<MemberFollow, Long> {

    // 특정 사용자가 다른 사용자를 팔로우하고 있는지 확인
    Optional<MemberFollow> findByFollowerAndFollowing(Member follower, Member following);

    // 특정 사용자가 다른 사용자를 팔로우하고 있는지 확인 (ID로)
    @Query("SELECT mf FROM MemberFollow mf WHERE mf.follower.id = :followerId AND mf.following.id = :followingId")
    Optional<MemberFollow> findByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    // 특정 사용자의 팔로워 목록 조회
    @Query("SELECT mf.follower FROM MemberFollow mf WHERE mf.following.id = :memberId")
    List<Member> findFollowersByMemberId(@Param("memberId") Long memberId);

    // 특정 사용자가 팔로우하는 사람 목록 조회
    @Query("SELECT mf.following FROM MemberFollow mf WHERE mf.follower.id = :memberId")
    List<Member> findFollowingsByMemberId(@Param("memberId") Long memberId);

    // 특정 사용자의 팔로워 수 조회
    @Query("SELECT COUNT(mf) FROM MemberFollow mf WHERE mf.following.id = :memberId")
    long countFollowersByMemberId(@Param("memberId") Long memberId);

    // 특정 사용자의 팔로잉 수 조회
    @Query("SELECT COUNT(mf) FROM MemberFollow mf WHERE mf.follower.id = :memberId")
    long countFollowingsByMemberId(@Param("memberId") Long memberId);

    // 팔로우 관계 존재 여부 확인
    boolean existsByFollowerAndFollowing(Member follower, Member following);

    // 팔로우 관계 존재 여부 확인 (ID로)
    @Query("SELECT CASE WHEN COUNT(mf) > 0 THEN true ELSE false END FROM MemberFollow mf WHERE mf.follower.id = :followerId AND mf.following.id = :followingId")
    boolean existsByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
} 