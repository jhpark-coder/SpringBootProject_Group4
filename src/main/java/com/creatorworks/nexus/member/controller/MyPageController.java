package com.creatorworks.nexus.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/User")
public class MyPageController {
    @GetMapping("/my-page")
    public String myPage() {
        return "member/myPage"; // myPage.html 템플릿을 보여줌
    }
}