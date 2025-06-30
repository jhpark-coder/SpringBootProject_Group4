package com.creatorworks.nexus.member.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.service.MemberFollowService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class MemberFollowController {

    private final MemberFollowService memberFollowService;

    /**
     * 팔로우/언팔로우 토글 API
     * @param followingId 팔로우할 사용자 ID
     * @param session HTTP 세션
     * @return 팔로우 상태 정보
     */
    @PostMapping("/{followingId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFollow(@PathVariable Long followingId, HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        
        if (loginUser == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인이 필요합니다.");
            errorResponse.put("message", "팔로우 기능을 사용하려면 로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            boolean isFollowing = memberFollowService.toggleFollow(loginUser.getId(), followingId);
            long followerCount = memberFollowService.getFollowerCount(followingId);
            long followingCount = memberFollowService.getFollowingCount(followingId);

            Map<String, Object> response = new HashMap<>();
            response.put("isFollowing", isFollowing);
            response.put("followerCount", followerCount);
            response.put("followingCount", followingCount);
            response.put("message", isFollowing ? "팔로우되었습니다." : "언팔로우되었습니다.");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "팔로우 실패");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 팔로우 상태 확인 API
     * @param followingId 확인할 사용자 ID
     * @param session HTTP 세션
     * @return 팔로우 상태 정보
     */
    @GetMapping("/{followingId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFollowStatus(@PathVariable Long followingId, HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        
        if (loginUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("isFollowing", false);
            response.put("followerCount", memberFollowService.getFollowerCount(followingId));
            response.put("followingCount", memberFollowService.getFollowingCount(followingId));
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(response);
        }

        boolean isFollowing = memberFollowService.isFollowing(loginUser.getId(), followingId);
        long followerCount = memberFollowService.getFollowerCount(followingId);
        long followingCount = memberFollowService.getFollowingCount(followingId);

        Map<String, Object> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        response.put("followerCount", followerCount);
        response.put("followingCount", followingCount);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 팔로워 목록 조회 API
     * @param memberId 사용자 ID
     * @return 팔로워 목록
     */
    @GetMapping("/{memberId}/followers")
    @ResponseBody
    public ResponseEntity<List<Member>> getFollowers(@PathVariable Long memberId) {
        List<Member> followers = memberFollowService.getFollowers(memberId);
        return ResponseEntity.ok(followers);
    }

    /**
     * 사용자의 팔로잉 목록 조회 API
     * @param memberId 사용자 ID
     * @return 팔로잉 목록
     */
    @GetMapping("/{memberId}/followings")
    @ResponseBody
    public ResponseEntity<List<Member>> getFollowings(@PathVariable Long memberId) {
        List<Member> followings = memberFollowService.getFollowings(memberId);
        return ResponseEntity.ok(followings);
    }

    /**
     * 사용자의 팔로우 통계 조회 API
     * @param memberId 사용자 ID
     * @return 팔로우 통계 정보
     */
    @GetMapping("/{memberId}/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFollowStats(@PathVariable Long memberId) {
        Map<String, Object> stats = memberFollowService.getFollowStats(memberId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 팔로워 목록 페이지
     * @param memberId 사용자 ID
     * @param model 모델
     * @return 팔로워 목록 페이지
     */
    @GetMapping("/{memberId}/followers/page")
    public String getFollowersPage(@PathVariable Long memberId, Model model) {
        List<Member> followers = memberFollowService.getFollowers(memberId);
        long followerCount = memberFollowService.getFollowerCount(memberId);
        
        model.addAttribute("followers", followers);
        model.addAttribute("followerCount", followerCount);
        model.addAttribute("memberId", memberId);
        
        return "member/followers";
    }

    /**
     * 팔로잉 목록 페이지
     * @param memberId 사용자 ID
     * @param model 모델
     * @return 팔로잉 목록 페이지
     */
    @GetMapping("/{memberId}/followings/page")
    public String getFollowingsPage(@PathVariable Long memberId, Model model) {
        List<Member> followings = memberFollowService.getFollowings(memberId);
        long followingCount = memberFollowService.getFollowingCount(memberId);
        
        model.addAttribute("followings", followings);
        model.addAttribute("followingCount", followingCount);
        model.addAttribute("memberId", memberId);
        
        return "member/followings";
    }
} 