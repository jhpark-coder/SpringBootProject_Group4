package com.creatorworks.nexus.auction.service;

import org.springframework.stereotype.Service;

import com.creatorworks.nexus.auction.dto.AuctionSaveRequest;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.repository.AuctionRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;

    public Auction saveAuction(AuctionSaveRequest request) {
        // 디버깅을 위한 로그 추가
        System.out.println("Saving auction: " + request.getName());
        System.out.println("Primary category: " + request.getPrimaryCategory());
        System.out.println("Secondary category: " + request.getSecondaryCategory());
        
        Auction auction = new Auction();
        auction.setName(request.getName());
        auction.setImageUrl(request.getImageUrl());
        auction.setPrimaryCategory(request.getPrimaryCategory());
        auction.setSecondaryCategory(request.getSecondaryCategory());
        auction.setDescription(request.getHtmlBackup()); // htmlBackup을 description으로 매핑
        auction.setTiptapJson(request.getTiptapJson());
        auction.setWorkDescription(request.getWorkDescription());
        auction.setBackgroundColor(request.getBackgroundColor());
        auction.setFontFamily(request.getFontFamily());
        auction.setAuctionDuration(request.getAuctionDuration());
        auction.setStartBidPrice(request.getStartBidPrice());
        auction.setBuyNowPrice(request.getBuyNowPrice());
        
        try {
            Auction saved = auctionRepository.save(auction);
            System.out.println("Auction saved successfully with ID: " + saved.getId());
            return saved;
        } catch (Exception e) {
            System.err.println("Error saving auction: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Auction updateAuction(Long id, AuctionSaveRequest request) {
        System.out.println("Updating auction with ID: " + id);
        
        Auction auction = findAuctionById(id);
        auction.setName(request.getName());
        auction.setImageUrl(request.getImageUrl());
        auction.setPrimaryCategory(request.getPrimaryCategory());
        auction.setSecondaryCategory(request.getSecondaryCategory());
        auction.setDescription(request.getHtmlBackup());
        auction.setTiptapJson(request.getTiptapJson());
        auction.setWorkDescription(request.getWorkDescription());
        auction.setBackgroundColor(request.getBackgroundColor());
        auction.setFontFamily(request.getFontFamily());
        auction.setAuctionDuration(request.getAuctionDuration());
        auction.setStartBidPrice(request.getStartBidPrice());
        auction.setBuyNowPrice(request.getBuyNowPrice());
        
        try {
            Auction updated = auctionRepository.save(auction);
            System.out.println("Auction updated successfully with ID: " + updated.getId());
            return updated;
        } catch (Exception e) {
            System.err.println("Error updating auction: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Auction findAuctionById(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid auction Id:" + id));
    }
}
