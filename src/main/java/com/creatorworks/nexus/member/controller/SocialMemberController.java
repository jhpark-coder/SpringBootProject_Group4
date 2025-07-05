package com.creatorworks.nexus.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.creatorworks.nexus.member.dto.SessionMemberFormDto;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.service.SocialMemberService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/social")
@Controller
@RequiredArgsConstructor
public class SocialMemberController {
    private final SocialMemberService socialMemberService;
    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    @GetMapping("/addInfo")
    public String socialMemberForm(Model model, @AuthenticationPrincipal OAuth2User oauthUser) {
        Boolean needsAdditionalInfo = (Boolean) httpSession.getAttribute("needsAdditionalInfo");
        if (needsAdditionalInfo == null || !needsAdditionalInfo || oauthUser == null) {
            return "redirect:/";
        }

        model.addAttribute("sessionMemberFormDto", new SessionMemberFormDto());
        return "member/socialMemberForm";
    }
    
    @PostMapping("/addInfo")
    public String socialMemberForm(@Valid SessionMemberFormDto sessionMemberFormDto, 
                                   BindingResult bindingResult, 
                                   Model model) {
        if (bindingResult.hasErrors()) {
            return "member/socialMemberForm";
        }
        
        try {
            String email = (String) httpSession.getAttribute("temp_oauth_email");
            if (email == null) {
                model.addAttribute("errorMessage", "세션이 만료되었거나 비정상적인 접근입니다.");
                return "member/socialMemberForm";
            }

            socialMemberService.completeSocialSignUp(email, sessionMemberFormDto);

            // 사용 완료한 임시 세션 정보 제거
            httpSession.removeAttribute("needsAdditionalInfo");
            httpSession.removeAttribute("temp_oauth_email");
            httpSession.removeAttribute("temp_oauth_attributes");
            
            return "redirect:/";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/socialMemberForm";
        }
    }
}
