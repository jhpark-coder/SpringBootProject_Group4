package com.creatorworks.nexus.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.creatorworks.nexus.member.dto.OAuthAttributesDto;
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

    @GetMapping("/addInfo")
    public String socialMemberForm(Model model) {
        // 세션에서 추가 정보 입력 필요 여부 확인
        Boolean needsAdditionalInfo = (Boolean) httpSession.getAttribute("needsAdditionalInfo");
        OAuthAttributesDto attributes = (OAuthAttributesDto)httpSession.getAttribute("temp_oauth_attributes");
        if (needsAdditionalInfo == null || !needsAdditionalInfo) {
            // 추가 정보가 필요하지 않으면 메인 페이지로 리다이렉트
            return "redirect:/";
        }
        SessionMemberFormDto formDto = new SessionMemberFormDto();
        if (attributes != null && attributes.getName() != null) {
            // 소셜 서비스에서 이름을 받아왔다면, 폼 DTO의 기본값으로 설정
            formDto.setName(attributes.getName());
        }

        // formDto를 모델에 추가
        model.addAttribute("sessionMemberFormDto", formDto);
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
