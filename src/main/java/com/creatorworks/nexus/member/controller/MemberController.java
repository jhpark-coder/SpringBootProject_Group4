package com.creatorworks.nexus.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.creatorworks.nexus.member.dto.EmailAuthRequestDto;
import com.creatorworks.nexus.member.dto.MemberFormDto;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

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

    // [변경 1] AJAX 요청을 처리할 새로운 메소드를 만듭니다.
    // @ResponseBody 어노테이션은 이 메소드의 반환값이 뷰(html)가 아닌, 응답 본문(body) 데이터임을 명시합니다.
    @PostMapping("/api/new") // URL을 '/new'와 구분하기 위해 '/api/new'로 변경
    @ResponseBody
    public ResponseEntity<?> registerMember(@Valid @RequestBody MemberFormDto memberFormDto, BindingResult bindingResult){
        // 1. 비밀번호와 비밀번호 확인이 일치하는지 먼저 검사
        if (!memberFormDto.getPassword().equals(memberFormDto.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "password.mismatch", "비밀번호가 일치하지 않습니다.");
        }
        // 2. 유효성 검사 에러가 하나라도 있다면
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            // FieldError 리스트를 순회하며 필드명과 에러 메시지를 Map에 담습니다.
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            // 400 Bad Request 상태와 함께 에러 Map을 JSON 형태로 반환
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        // 3. 중복 회원 검사 등 서비스 로직 수행
        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("globalError", e.getMessage()); // 이메일 중복 같은 전역 에러
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        // 4. 모든 검사를 통과하면 성공 메시지와 함께 200 OK 상태 반환
        return new ResponseEntity<>("회원가입이 성공적으로 완료되었습니다.", HttpStatus.OK);
    }


//    @PostMapping("/new")
//    public String memberForm(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {
//        System.out.println("들어는 옴");
//        if (bindingResult.hasErrors()) {
//            System.out.println("입력값 오류");
//            return "member/memberForm";
//        }
//        if (!memberFormDto.getPassword().equals(memberFormDto.getPasswordConfirm())) {
//            System.out.println("비밀번호 일치 하지않음");
//            // bindingResult에 직접 에러를 추가하여 Thymeleaf에 전달
//            bindingResult.rejectValue("passwordConfirm", "password.mismatch", "비밀번호가 일치하지 않습니다.");
//            return "member/memberForm";
//        }
//        try {
//            Member member = Member.createMember(memberFormDto, passwordEncoder);
//            memberService.saveMember(member);
//        } catch (IllegalStateException e) {
//            System.out.println("Service 못보냄");
//            model.addAttribute("errorMessage", e.getMessage());
//            return "member/memberForm";
//        }
//        return "redirect:/";
//    }
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
