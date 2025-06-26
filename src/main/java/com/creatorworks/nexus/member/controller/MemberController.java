package com.creatorworks.nexus.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "member/loginForm";
    }

    // 로그인 처리
    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login(@RequestParam String username, 
                                   @RequestParam String password, 
                                   HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Member> memberOpt = memberRepository.findByUsername(username);
            
            if (memberOpt.isPresent() && memberOpt.get().getPassword().equals(password)) {
                Member member = memberOpt.get();
                session.setAttribute("loginUser", member);
                
                response.put("success", true);
                response.put("message", "로그인 성공");
                response.put("username", member.getUsername());
                response.put("nickname", member.getNickname());
                
                System.out.println("로그인 성공: " + username);
            } else {
                response.put("success", false);
                response.put("message", "아이디 또는 비밀번호가 잘못되었습니다.");
                System.out.println("로그인 실패: " + username);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그인 중 오류가 발생했습니다: " + e.getMessage());
            System.out.println("로그인 오류: " + e.getMessage());
        }
        
        return response;
    }

    // 로그아웃
    @PostMapping("/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그아웃되었습니다.");
        
        System.out.println("로그아웃 완료");
        return response;
    }

    // 현재 로그인 상태 확인
    @GetMapping("/api/auth/status")
    @ResponseBody
    public Map<String, Object> getAuthStatus(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser != null) {
            response.put("loggedIn", true);
            response.put("username", loginUser.getUsername());
            response.put("nickname", loginUser.getNickname());
        } else {
            response.put("loggedIn", false);
        }
        
        return response;
    }
}
