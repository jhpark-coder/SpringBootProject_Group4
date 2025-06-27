package com.creatorworks.nexus.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.creatorworks.nexus.member.dto.EmailAuthRequestDto;
import com.creatorworks.nexus.member.dto.MemberFormDto;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/new")
    public String memberForm(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }

    @PostMapping("/new")
    public String memberForm(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {
        System.out.println("들어는 옴");
        if (bindingResult.hasErrors()) {
            System.out.println("입력값 오류");
            return "member/memberForm";
        }
        if (!memberFormDto.getPassword().equals(memberFormDto.getPasswordConfirm())) {
            System.out.println("비밀번호 일치 하지않음");
            // bindingResult에 직접 에러를 추가하여 Thymeleaf에 전달
            bindingResult.rejectValue("passwordConfirm", "password.mismatch", "비밀번호가 일치하지 않습니다.");
            return "member/memberForm";
        }
        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            System.out.println("Service 못보냄");
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }
        return "redirect:/";
    }
    @PostMapping("/email-auth")
    public ResponseEntity<String> sendAuthEmail(@RequestBody EmailAuthRequestDto requestDto) {
        try {
            memberService.sendAuthEmail(requestDto.getEmail());
            System.out.println("인증메일이 발송되었습니다.");
            return new ResponseEntity<>("인증 메일이 발송되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("메일 발송에 실패했습니다.");
            return new ResponseEntity<>("메일 발송에 실패했습니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/email-verify")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailAuthRequestDto requestDto) {
        boolean isVerified = memberService.verifyEmail(requestDto);
        if (isVerified) {
            return new ResponseEntity<>("이메일 인증이 성공하였습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("인증 코드가 일치하지 않거나 만료되었습니다.", HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/login")
    public String login() {
        return "member/loginForm";
    }
    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg","이메일(ID) 또는 비밀번호를 확인해주세요");
        return "member/loginForm";
    }

}
