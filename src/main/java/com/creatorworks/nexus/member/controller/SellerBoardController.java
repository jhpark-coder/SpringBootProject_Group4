package com.creatorworks.nexus.member.controller;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.service.SellerBoardService;
import com.creatorworks.nexus.product.entity.ProductInquiry;
import com.creatorworks.nexus.product.entity.ProductReview;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerBoardController {

    private final MemberRepository memberRepository;
    private final SellerBoardService sellerBoardService;

    @GetMapping("/reviews")
    public String reviewBoard(Principal principal, Pageable pageable, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Member seller = memberRepository.findByEmail(principal.getName());
        Page<ProductReview> reviewPage = sellerBoardService.getReviewsBySeller(seller, pageable);
        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("Name", seller.getName());
        return "seller/reviewBoard";
    }

    @GetMapping("/inquiries")
    public String inquiryBoard(Principal principal, Pageable pageable, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Member seller = memberRepository.findByEmail(principal.getName());
        Page<ProductInquiry> inquiryPage = sellerBoardService.getInquiriesBySeller(seller, pageable);
        model.addAttribute("inquiryPage", inquiryPage);
        model.addAttribute("Name", seller.getName());
        return "seller/inquiryBoard";
    }
} 