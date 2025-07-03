package com.creatorworks.nexus.member.controller;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.service.SellerBoardService;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductInquiry;
import com.creatorworks.nexus.product.entity.ProductReview;
import com.creatorworks.nexus.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final ProductService productService;

    @GetMapping("/products")
    public String productBoard(Principal principal, @PageableDefault(sort = "regTime", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Member seller = memberRepository.findByEmail(principal.getName());
        Page<Product> productPage = productService.findProductsBySeller(seller, pageable);
        model.addAttribute("productPage", productPage);
        model.addAttribute("Name", seller.getName());
        addPaginationAttributes(model, productPage);
        return "seller/myProducts";
    }

    @GetMapping("/reviews")
    public String reviewBoard(Principal principal, @PageableDefault(sort = "regTime", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Member seller = memberRepository.findByEmail(principal.getName());
        Page<ProductReview> reviewPage = sellerBoardService.getReviewsBySeller(seller, pageable);
        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("Name", seller.getName());
        addPaginationAttributes(model, reviewPage);
        return "seller/reviewBoard";
    }

    @GetMapping("/inquiries")
    public String inquiryBoard(Principal principal, @PageableDefault(sort = "regTime", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Member seller = memberRepository.findByEmail(principal.getName());
        Page<ProductInquiry> inquiryPage = sellerBoardService.getInquiriesBySeller(seller, pageable);
        model.addAttribute("inquiryPage", inquiryPage);
        model.addAttribute("Name", seller.getName());
        addPaginationAttributes(model, inquiryPage);
        return "seller/inquiryBoard";
    }
    
    private void addPaginationAttributes(Model model, Page<?> page) {
        int totalPages = page.getTotalPages();
        if (totalPages > 0) {
            int currentPage = page.getNumber();
            int pageWindowSize = 10;
            int startPage = Math.max(0, (currentPage / pageWindowSize) * pageWindowSize);
            int endPage = Math.min(totalPages - 1, startPage + pageWindowSize - 1);
            
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
        }
    }
} 