package com.creatorworks.nexus.member.controller;

import com.creatorworks.nexus.member.dto.CustomUserDetails;
import com.creatorworks.nexus.member.dto.MemberModifyDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
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

import java.security.Principal;
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


//    @PostMapping("/new") //기존 새로고침 방식
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

    @GetMapping("/modify")
    public String memberModify(@AuthenticationPrincipal CustomUserDetails user, Model model){
        String currentEmail = user.getUsername();

        // 1-2. 이메일을 이용해 DB에서 전체 회원 정보를 조회합니다.
        Member member = memberService.findByEmail(currentEmail);

        // 1-3. 조회된 엔티티 정보를 DTO로 변환합니다.
        //      (비밀번호 같은 민감 정보는 DTO에 담지 않습니다.)
        MemberModifyDto memberModifyDto = new MemberModifyDto();
        memberModifyDto.setName(member.getName());
        memberModifyDto.setEmail(member.getEmail());
        memberModifyDto.setGender(member.getGender());
        // 생년월일 필드가 Member 엔티티와 DTO에 모두 String으로 되어 있다고 가정
        memberModifyDto.setBirthYear(member.getBirthYear());
        memberModifyDto.setBirthMonth(member.getBirthMonth());
        memberModifyDto.setBirthDay(member.getBirthDay());

        // 1-4. DTO를 Model에 담아 View로 전달합니다.
        model.addAttribute("memberModifyDto", memberModifyDto);

        // 1-5. 수정 폼 페이지의 경로를 반환합니다. (memberForm.html을 재사용)
        return "member/memberModify";
    }
    @PostMapping("/modify")
    public String memberUpdate(@Valid MemberModifyDto memberModifyDto, BindingResult bindingResult,
                               @AuthenticationPrincipal CustomUserDetails  user, Model model) {

        // 2-1. @Valid를 통한 기본 유효성 검증 (별명, 성별, 생년월일 등)
        if (bindingResult.hasErrors()) {
            // 이메일 필드는 readonly 이지만, 폼 제출 시 함께 넘어오므로 DTO에 다시 세팅해줍니다.
            // 이렇게 하지 않으면 유효성 검증 실패 후 리프레시될 때 이메일 필드가 비어있게 됩니다.
            memberModifyDto.setEmail(user.getUsername());
            System.out.println("firstError");
            return "member/memberModify";
        }

        try {
            // 2-2. 서비스 계층에 DTO와 현재 사용자 이메일을 넘겨 업데이트 로직을 수행합니다.
            memberService.updateMember(memberModifyDto, user.getUsername());
        } catch (IllegalStateException e) {
            // 2-3. 서비스 로직에서 발생할 수 있는 예외 처리 (예: 중복된 별명 등)
            model.addAttribute("errorMessage", e.getMessage());
            memberModifyDto.setEmail(user.getUsername()); // 오류 발생 시에도 이메일 필드 유지를 위해 추가
            System.out.println("secondError");
            return "member/memberModify";
        }

        // 2-4. 수정이 성공적으로 완료되면 마이페이지 등으로 리다이렉트합니다.
        return "/User/my-Page"; // 예시 경로입니다.
    }

}
