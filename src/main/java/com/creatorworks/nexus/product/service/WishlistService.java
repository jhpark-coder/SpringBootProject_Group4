package com.creatorworks.nexus.product.service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.Wishlist;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.product.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    // 찜하기 추가
    @Transactional
    public boolean addToWishlist(Long memberId, Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            
            // 임시로 Member 객체 생성 (실제로는 MemberService에서 가져와야 함)
            Member member = new Member();
            member.setId(memberId);
            
            // 이미 찜했는지 확인
            if (wishlistRepository.existsByMemberAndProduct(member, product)) {
                return false; // 이미 찜한 상품
            }
            
            // 찜하기 추가
            Wishlist wishlist = new Wishlist(member, product);
            wishlistRepository.save(wishlist);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 찜하기 제거
    @Transactional
    public boolean removeFromWishlist(Long memberId, Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            
            // 임시로 Member 객체 생성
            Member member = new Member();
            member.setId(memberId);
            
            // 찜하기 찾기
            Wishlist wishlist = wishlistRepository.findByMemberAndProduct(member, product)
                    .orElse(null);
            
            if (wishlist != null) {
                wishlistRepository.delete(wishlist);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 찜하기 상태 확인
    public boolean isWished(Long memberId, Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            
            Member member = new Member();
            member.setId(memberId);
            
            return wishlistRepository.existsByMemberAndProduct(member, product);
        } catch (Exception e) {
            return false;
        }
    }

    // 사용자의 찜한 상품 목록 조회
    public List<Product> getWishedProducts(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return wishlistRepository.findWishedProductsByMember(member);
    }

    // 사용자의 찜하기 개수 조회
    public long getWishlistCount(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return wishlistRepository.countByMember(member);
    }

    // 상품의 찜하기 개수 조회
    public long getProductWishCount(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return wishlistRepository.countByProduct(product);
    }

    // 찜하기 토글 (찜하기/찜해제)
    @Transactional
    public boolean toggleWishlist(Long memberId, Long productId) {
        if (isWished(memberId, productId)) {
            return removeFromWishlist(memberId, productId);
        } else {
            return addToWishlist(memberId, productId);
        }
    }
} 