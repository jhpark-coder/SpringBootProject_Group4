package com.creatorworks.nexus.member.controller;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.service.MemberFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class MemberFollowController {

    private final MemberFollowService memberFollowService;

    /**
     * 팔로우/언팔로우 토글 API
     * @param toUserId 팔로우할 사용자 ID
     * @param session HTTP 세션
     * @return 팔로우 상태 정보
     */
    @PostMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> toggleFollow(
            @PathVariable("userId") Long toUserId,
            HttpSession session) {

        // 1. 로그인하지 않은 경우 예외 처리 (테스트를 위해 주석 처리)
        /*
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인이 필요합니다.");
            errorResponse.put("message", "팔로우 기능을 사용하려면 로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        */

        // 2. 테스트용 사용자 ID (실제로는 loginUser.getId() 사용)
        Long fromUserId = 1L; // 테스트용 사용자 ID
        
        // 3. 본인에게 구독할 수 없도록 체크
        if (fromUserId.equals(toUserId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "본인 구독 불가");
            errorResponse.put("message", "자기 자신을 구독할 수 없습니다.");
            System.out.println("=== 본인 구독 시도 차단 ===");
            System.out.println("사용자 ID: " + fromUserId + "가 본인을 구독하려고 시도");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        System.out.println("=== 팔로우 토글 테스트 ===");
        System.out.println("팔로우하는 사용자 ID: " + fromUserId);
        System.out.println("팔로우받는 사용자 ID: " + toUserId);

        // 4. 서비스 호출하여 핵심 로직 수행
        Map<String, Object> result = memberFollowService.toggleFollow(fromUserId, toUserId);

        System.out.println("팔로우 토글 결과: " + result);

        // 5. 결과 반환
        return ResponseEntity.ok(result);
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
        // 로그인 체크 (테스트를 위해 주석 처리)
        /*
        Member loginUser = (Member) session.getAttribute("loginUser");
        
        if (loginUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("isFollowing", false);
            response.put("followerCount", memberFollowService.getFollowerCount(followingId));
            response.put("followingCount", memberFollowService.getFollowingCount(followingId));
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(response);
        }
        */

        // 테스트용 사용자 ID (실제로는 loginUser.getId() 사용)
        Long fromUserId = 1L;
        
        System.out.println("=== 팔로우 상태 확인 ===");
        System.out.println("확인하는 사용자 ID: " + fromUserId);
        System.out.println("확인할 사용자 ID: " + followingId);

        boolean isFollowing = memberFollowService.isFollowing(fromUserId, followingId);
        long followerCount = memberFollowService.getFollowerCount(followingId);
        long followingCount = memberFollowService.getFollowingCount(followingId);

        Map<String, Object> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        response.put("followerCount", followerCount);
        response.put("followingCount", followingCount);

        System.out.println("팔로우 상태 확인 결과: " + response);
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