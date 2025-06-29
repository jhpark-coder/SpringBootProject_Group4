package com.creatorworks.nexus.auction.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.auction.dto.AuctionSaveRequest;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.service.AuctionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auctions")
public class AuctionController {
    private final AuctionService auctionService;

    @GetMapping("")
    public String auctionPage() {
        return "auction";
    }

    @PostMapping("/api/auctions")
    @ResponseBody
    public Long saveAuction(@RequestBody AuctionSaveRequest request, Principal principal) {
        String userEmail = principal.getName();
        Auction saved = auctionService.saveAuction(request, userEmail);
        return saved.getId();
    }

    @PutMapping("/api/auctions/{id}")
    @ResponseBody
    public Long updateAuction(@PathVariable Long id, @RequestBody AuctionSaveRequest request, Principal principal) {
        String userEmail = principal.getName();
        Auction updated = auctionService.updateAuction(id, request, userEmail);
        return updated.getId();
    }

    @GetMapping("/api/auctions/{id}")
    @ResponseBody
    public Auction getAuction(@PathVariable Long id) {
        return auctionService.findAuctionById(id);
    }

    @GetMapping("/result/auction/{id}")
    public String auctionResult(@PathVariable Long id, Model model) {
        Auction auction = auctionService.findAuctionById(id);
        model.addAttribute("auction", auction);
        return "auction/auctionResult";
    }
}
