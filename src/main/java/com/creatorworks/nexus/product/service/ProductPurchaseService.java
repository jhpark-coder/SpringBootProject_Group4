package com.creatorworks.nexus.product.service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductPurchaseService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    /**
     * 포인트로 상품 구매
     */
    public Map<String, Object> purchaseWithPoints(Long productId, Integer price, String userEmail) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 상품 조회
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
            
            // 사용자 조회
            Member buyer = memberRepository.findByEmail(userEmail);
            if (buyer == null) {
                result.put("success", false);
                result.put("message", "사용자를 찾을 수 없습니다.");
                return result;
            }

            
            // 이미 구매했는지 확인
            if (orderRepository.existsByBuyerAndProduct(buyer, product)) {
                result.put("success", false);
                result.put("message", "이미 구매한 상품입니다.");
                return result;
            }
            
            // 포인트 차감 (실제 포인트 시스템 구현 필요)
            // 여기서는 간단히 처리
            if (buyer.getPoint() == null || buyer.getPoint() < price) {
                result.put("success", false);
                result.put("message", "포인트가 부족합니다.");
                return result;
            }
            
            // 포인트 차감
            buyer.setPoint(buyer.getPoint() - price);
            memberRepository.save(buyer);
            
            // 주문 생성
            Order order = Order.builder()
                    .buyer(buyer)
                    .product(product)
                    .build();
            orderRepository.save(order);
            
            result.put("success", true);
            result.put("message", "포인트 결제가 완료되었습니다.");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 정기구독 신청
     */
    public Map<String, Object> subscribeToAuthor(Long productId, Long authorId, String userEmail) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 상품 조회
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
            
            // 사용자 조회
            Member subscriber = memberRepository.findByEmail(userEmail);
            if (subscriber == null) {
                result.put("success", false);
                result.put("message", "사용자를 찾을 수 없습니다.");
                return result;
            }
            
            // 작가 조회
            Member author = memberRepository.findById(authorId)
                    .orElseThrow(() -> new RuntimeException("작가를 찾을 수 없습니다."));
            
            // 자기 자신을 구독하려는 경우
            if (subscriber.getId().equals(authorId)) {
                result.put("success", false);
                result.put("message", "자기 자신을 구독할 수 없습니다.");
                return result;
            }
            
            // 이미 구독 중인지 확인 (실제 구독 시스템 구현 필요)
            // 여기서는 간단히 처리
            boolean alreadySubscribed = false; // 실제 구독 확인 로직 필요
            
            if (alreadySubscribed) {
                result.put("success", false);
                result.put("message", "이미 구독 중인 작가입니다.");
                return result;
            }
            
            // 구독 처리 (실제 구독 시스템 구현 필요)
            // 여기서는 성공으로 처리
            result.put("success", true);
            result.put("message", "정기구독 신청이 완료되었습니다.");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "구독 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
} 