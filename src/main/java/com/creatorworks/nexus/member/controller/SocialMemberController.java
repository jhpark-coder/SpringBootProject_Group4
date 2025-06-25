package com.creatorworks.nexus.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.creatorworks.nexus.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/social")
@Controller
@RequiredArgsConstructor
public class SocialMemberController {
    private final MemberService memberService;
    
}
