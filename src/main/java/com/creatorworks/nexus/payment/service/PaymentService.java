package com.creatorworks.nexus.payment.service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.payment.dto.PaymentRequestDto;
import com.creatorworks.nexus.payment.dto.PaymentResponseDto;
import com.creatorworks.nexus.payment.entity.Payment;
import com.creatorworks.nexus.payment.repository.PaymentRepository;
import com.creatorworks.nexus.point.service.PointService;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final PointService pointService;
    private final MemberRepository memberRepository;
    
    /**
     * 포인트 결제 처리
     */
    public PaymentResponseDto processPointPayment(String email, PaymentRequestDto requestDto) {
        // 사용자 정보 조회
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // 상품 정보 조회
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        
        // 포인트 잔액 확인 및 차감
        boolean success = pointService.usePoints(
                member.getId(),
                requestDto.getAmount(),
                requestDto.getDescription(),
                product.getId(),
                "PRODUCT"
        );
        
        if (!success) {
            throw new RuntimeException("포인트가 부족합니다.");
        }
        
        // 결제 정보 저장
        Payment payment = new Payment();
        payment.setMember(member);
        payment.setProduct(product);
        payment.setMethod(Payment.PaymentMethod.POINT);
        payment.setAmount(requestDto.getAmount());
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setDescription(requestDto.getDescription());
        payment.setTransactionId("POINT_" + System.currentTimeMillis());
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // 응답 DTO 생성
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setPaymentId(savedPayment.getId());
        responseDto.setProductId(product.getId());
        responseDto.setProductName(product.getName());
        responseDto.setAmount(savedPayment.getAmount());
        responseDto.setPaymentMethod(savedPayment.getMethod().name());
        responseDto.setStatus(savedPayment.getStatus().name());
        responseDto.setPaymentDate(savedPayment.getPaymentDate());
        responseDto.setDescription(savedPayment.getDescription());
        responseDto.setRemainingBalance(pointService.getBalance(email));
        
        return responseDto;
    }
    
    /**
     * 결제 이력 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentHistory(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));
        
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setPaymentId(payment.getId());
        responseDto.setProductId(payment.getProduct().getId());
        responseDto.setProductName(payment.getProduct().getName());
        responseDto.setAmount(payment.getAmount());
        responseDto.setPaymentMethod(payment.getMethod().name());
        responseDto.setStatus(payment.getStatus().name());
        responseDto.setPaymentDate(payment.getPaymentDate());
        responseDto.setDescription(payment.getDescription());
        
        return responseDto;
    }
} 