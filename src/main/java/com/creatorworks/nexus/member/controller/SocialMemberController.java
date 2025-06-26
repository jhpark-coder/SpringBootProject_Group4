package com.creatorworks.nexus.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.creatorworks.nexus.member.dto.SessionMemberDto;
import com.creatorworks.nexus.member.dto.SessionMemberFormDto;
import com.creatorworks.nexus.member.service.SocialMemberService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/social")
@Controller
@RequiredArgsConstructor
public class SocialMemberController {
    private final SocialMemberService socialMemberService;
    private final HttpSession httpSession;

    @GetMapping("/addinfo")
    public String socialMemberForm(Model model) {
        // 세션에서 추가 정보 입력 필요 여부 확인
        Boolean needsAdditionalInfo = (Boolean) httpSession.getAttribute("needsAdditionalInfo");
                
        if (needsAdditionalInfo == null || !needsAdditionalInfo) {
            // 추가 정보가 필요하지 않으면 메인 페이지로 리다이렉트
            return "redirect:/";
        }
        
        model.addAttribute("sessionMemberFormDto", new SessionMemberFormDto());
        return "member/socialMemberForm";
    }
    
    @PostMapping("/addinfo")
    public String socialMemberForm(@Valid SessionMemberFormDto sessionMemberFormDto, 
                                   BindingResult bindingResult, 
                                   Model model) {
        if (bindingResult.hasErrors()) {
            return "member/socialMemberForm";
        }
        
        try {
            SessionMemberDto sessionMember = (SessionMemberDto) httpSession.getAttribute("member");
            if (sessionMember == null) {
                model.addAttribute("errorMessage", "로그인 정보를 찾을 수 없습니다.");
                return "member/socialMemberForm";
            }
            
            socialMemberService.updateSocialMemberInfo(sessionMember.getEmail(), sessionMemberFormDto);
            
            // 추가 정보 입력 완료 플래그 제거
            httpSession.removeAttribute("needsAdditionalInfo");
            
            return "redirect:/";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/socialMemberForm";
        }
    }
}
