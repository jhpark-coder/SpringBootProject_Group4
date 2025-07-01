//package com.creatorworks.nexus.order.controller;
//
//import com.creatorworks.nexus.member.dto.CustomUserDetails;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model; // ✅ 여기 수정
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Controller
//@RequestMapping("/popup")
//@RequiredArgsConstructor
//public class OrderController {
//
//    @GetMapping("/requestPay")
//    public String requestPay(
//            @RequestParam String item,
//            @RequestParam String immediatly,
//            @RequestParam String actionBoardNo,
//            @AuthenticationPrincipal CustomUserDetails principal,
//            Model model) {
//
//        model.addAttribute("actionBoardItemName", item);
//        model.addAttribute("immediatly", immediatly);
//        model.addAttribute("actionBoardNo", actionBoardNo);
//        model.addAttribute("name", principal.getName());
//        model.addAttribute("email", principal.getUsername());
//
//        return "popup/requestPay";
//    }
//}