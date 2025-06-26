package com.creatorworks.nexus.product.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.entity.ProductHeart;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.repository.ProductHeartRepository;
import com.creatorworks.nexus.product.dto.ProductSaveRequest;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductHeartRepository productHeartRepository;

    @Cacheable(value = "products", key = "#pageable")
    public Page<Product> findAllProducts(Pageable pageable) {
        System.out.println("DB에서 상품 목록을 조회합니다. page=" + pageable.getPageNumber());
        return productRepository.findAll(pageable);
    }

    // ProductService.java
public Product findProductById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. id=" + id));
}
    public Product saveProduct(ProductSaveRequest request) {
        System.out.println("=== 상품 저장 요청 데이터 ===");
        System.out.println("이름: " + request.getName());
        System.out.println("가격: " + request.getPrice());
        System.out.println("TiptapJson 길이: " + (request.getTiptapJson() != null ? request.getTiptapJson().length() : "null"));
        System.out.println("TiptapJson 내용: " + request.getTiptapJson());
        System.out.println("HtmlBackup 길이: " + (request.getHtmlBackup() != null ? request.getHtmlBackup().length() : "null"));
        System.out.println("배경색: " + request.getBackgroundColor());
        System.out.println("폰트: " + request.getFontFamily());
        System.out.println("===========================");
        
        Product product = new Product();
        product.setName(request.getName());
        product.setImageUrl(request.getImageUrl());
        product.setPrimaryCategory(request.getPrimaryCategory());
        product.setSecondaryCategory(request.getSecondaryCategory());
        product.setDescription(request.getHtmlBackup());
        product.setTiptapJson(request.getTiptapJson());
        product.setBackgroundColor(request.getBackgroundColor());
        product.setFontFamily(request.getFontFamily());
        product.setPrice(request.getPrice());
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductSaveRequest request) {
        System.out.println("Updating product with ID: " + id);
        
        Product product = findProductById(id);
        product.setName(request.getName());
        product.setImageUrl(request.getImageUrl());
        product.setPrimaryCategory(request.getPrimaryCategory());
        product.setSecondaryCategory(request.getSecondaryCategory());
        product.setDescription(request.getHtmlBackup());
        product.setTiptapJson(request.getTiptapJson());
        product.setBackgroundColor(request.getBackgroundColor());
        product.setFontFamily(request.getFontFamily());
        product.setPrice(request.getPrice());
        
        try {
            Product updated = productRepository.save(product);
            System.out.println("Product updated successfully with ID: " + updated.getId());
            return updated;
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 좋아요 토글 메소드 (개선된 버전)
    public boolean toggleHeart(Long productId, String username) {
        System.out.println("=== ProductService.toggleHeart() ===");
        System.out.println("상품 ID: " + productId);
        System.out.println("사용자명: " + username);
        
        Product product = findProductById(productId);
        System.out.println("상품명: " + product.getName());
        
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        System.out.println("멤버 ID: " + member.getId());
        
        // 이미 좋아요를 눌렀는지 확인 (더 효율적인 쿼리)
        Optional<ProductHeart> existingHeart = productHeartRepository.findByMemberIdAndProductId(member.getId(), productId);
        
        if (existingHeart.isPresent()) {
            // 좋아요 취소
            System.out.println("기존 좋아요 발견 - 좋아요 취소");
            productHeartRepository.delete(existingHeart.get());
            System.out.println("좋아요 취소 완료");
            return false;
        } else {
            // 좋아요 추가
            System.out.println("새로운 좋아요 추가");
            ProductHeart heart = new ProductHeart();
            heart.setMember(member);
            heart.setProduct(product);
            productHeartRepository.save(heart);
            System.out.println("좋아요 추가 완료");
            return true;
        }
    }
    
    // 사용자가 특정 상품을 좋아요했는지 확인하는 메소드
    public boolean isLikedByUser(Long productId, String username) {
        try {
            Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
            
            Optional<ProductHeart> existingHeart = productHeartRepository.findByMemberIdAndProductId(member.getId(), productId);
            return existingHeart.isPresent();
        } catch (Exception e) {
            System.out.println("좋아요 상태 확인 중 오류: " + e.getMessage());
            return false;
        }
    }
    
    // 특정 상품의 좋아요 개수를 효율적으로 조회하는 메소드
    public long getHeartCount(Long productId) {
        try {
            long count = productHeartRepository.countByProductId(productId);
            System.out.println("상품 ID " + productId + "의 좋아요 개수: " + count);
            return count;
        } catch (Exception e) {
            System.out.println("좋아요 개수 조회 중 오류: " + e.getMessage());
            return 0;
        }
    }
    
    // 모든 상품의 좋아요 개수를 한 번에 조회하는 메소드 (성능 최적화용)
    public Map<Long, Long> getHeartCountsForProducts(List<Long> productIds) {
        Map<Long, Long> heartCounts = new HashMap<>();
        for (Long productId : productIds) {
            heartCounts.put(productId, getHeartCount(productId));
        }
        return heartCounts;
    }
}
