package com.creatorworks.nexus.member.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.member.dto.EmailAuthRequestDto;
import com.creatorworks.nexus.member.dto.MemberFormDto;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/api/new")
    @ResponseBody
    public ResponseEntity<?> registerMember(@Valid @RequestBody MemberFormDto memberFormDto, BindingResult bindingResult){
        if (!memberFormDto.getPassword().equals(memberFormDto.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "password.mismatch", "비밀번호가 일치하지 않습니다.");
        }
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("globalError", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("회원가입이 성공적으로 완료되었습니다.", HttpStatus.OK);
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
    public String login(@RequestParam(value = "message", required = false) String message, 
                       Model model, HttpServletRequest request) {
        if (message != null) {
            model.addAttribute("message", message);
            // 권한 업데이트 메시지인 경우 기존 세션 무효화
            if (message.contains("권한이 업데이트되었습니다")) {
                request.getSession().invalidate();
            }
        }
        return "member/loginForm";
    }
    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg","이메일(ID) 또는 비밀번호를 확인해주세요");
        return "member/loginForm";
    }

    @GetMapping("/seller-register")
    public String sellerRegisterPage() {
        return "member/seller-register";
    }
}
