package com.creatorworks.nexus.subscription.service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.payment.entity.Payment;
import com.creatorworks.nexus.payment.repository.PaymentRepository;
import com.creatorworks.nexus.point.service.PointService;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.subscription.dto.SubscriptionRequestDto;
import com.creatorworks.nexus.subscription.dto.SubscriptionResponseDto;
import com.creatorworks.nexus.subscription.entity.Subscription;
import com.creatorworks.nexus.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final PointService pointService;
    
    /**
     * 구독 생성 (포인트 결제)
     */
    public SubscriptionResponseDto createSubscription(String email, SubscriptionRequestDto requestDto) {
        // 사용자 정보 조회
        Member subscriber = memberRepository.findByEmail(email);
        if (subscriber == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // 작가 정보 조회
        Member author = memberRepository.findById(requestDto.getAuthorId())
                .orElseThrow(() -> new RuntimeException("작가를 찾을 수 없습니다."));
        
        // 상품 정보 조회
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        
        // 이미 구독 중인지 확인
        subscriptionRepository.findBySubscriberIdAndAuthorId(subscriber.getId(), author.getId())
                .ifPresent(subscription -> {
                    if (subscription.isActive()) {
                        throw new RuntimeException("이미 구독 중인 작가입니다.");
                    }
                });
        
        // 포인트 결제 처리
        if ("POINT".equals(requestDto.getPaymentMethod())) {
            boolean success = pointService.usePoints(
                    subscriber.getId(),
                    requestDto.getMonthlyPrice(),
                    "구독 결제: " + author.getName() + " 작가",
                    product.getId(),
                    "SUBSCRIPTION"
            );
            
            if (!success) {
                throw new RuntimeException("포인트가 부족합니다.");
            }
        }
        
        // 구독 정보 생성
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusMonths(1);
        LocalDateTime nextBillingDate = endDate;
        
        Subscription subscription = new Subscription();
        subscription.setSubscriber(subscriber);
        subscription.setAuthor(author);
        subscription.setProduct(product);
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setMonthlyPrice(requestDto.getMonthlyPrice());
        subscription.setStartDate(now);
        subscription.setEndDate(endDate);
        subscription.setNextBillingDate(nextBillingDate);
        subscription.setAutoRenewal(requestDto.getAutoRenewal());
        
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        
        // 결제 정보 저장
        Payment payment = new Payment();
        payment.setMember(subscriber);
        payment.setProduct(product);
        payment.setMethod(Payment.PaymentMethod.POINT);
        payment.setAmount(requestDto.getMonthlyPrice());
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentDate(now);
        payment.setDescription("구독 결제: " + author.getName() + " 작가");
        payment.setTransactionId("SUB_" + System.currentTimeMillis());
        
        paymentRepository.save(payment);
        
        return createResponseDto(savedSubscription);
    }
    
    /**
     * 구독 취소
     */
    public SubscriptionResponseDto cancelSubscription(String email, Long subscriptionId, String reason) {
        Member subscriber = memberRepository.findByEmail(email);
        if (subscriber == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("구독을 찾을 수 없습니다."));
        
        // 구독자 본인인지 확인
        if (!subscription.getSubscriber().getId().equals(subscriber.getId())) {
            throw new RuntimeException("구독을 취소할 권한이 없습니다.");
        }
        
        subscription.cancel(reason);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        
        return createResponseDto(savedSubscription);
    }
    
    /**
     * 구독자별 구독 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<SubscriptionResponseDto> getSubscriptionsBySubscriber(String email, Pageable pageable) {
        Member subscriber = memberRepository.findByEmail(email);
        if (subscriber == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        Page<Subscription> subscriptions = subscriptionRepository
                .findBySubscriberIdOrderByRegTimeDesc(subscriber.getId(), pageable);
        
        return subscriptions.map(this::createResponseDto);
    }
    
    /**
     * 작가별 구독자 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<SubscriptionResponseDto> getSubscriptionsByAuthor(Long authorId, Pageable pageable) {
        Page<Subscription> subscriptions = subscriptionRepository
                .findByAuthorIdOrderByRegTimeDesc(authorId, pageable);
        
        return subscriptions.map(this::createResponseDto);
    }
    
    /**
     * 구독 갱신 처리 (크론잡용)
     */
    public void processSubscriptionRenewals() {
        log.info("구독 갱신 처리를 시작합니다.");
        
        List<Subscription> subscriptionsForRenewal = subscriptionRepository
                .findSubscriptionsForRenewal(LocalDateTime.now());
        
        for (Subscription subscription : subscriptionsForRenewal) {
            try {
                // 포인트 잔액 확인
                Integer balance = pointService.getBalance(subscription.getSubscriber().getEmail());
                
                if (balance >= subscription.getMonthlyPrice()) {
                    // 포인트 차감
                    boolean success = pointService.usePoints(
                            subscription.getSubscriber().getId(),
                            subscription.getMonthlyPrice(),
                            "구독 자동 갱신: " + subscription.getAuthor().getName() + " 작가",
                            subscription.getProduct().getId(),
                            "SUBSCRIPTION_RENEWAL"
                    );
                    
                    if (success) {
                        // 구독 갱신
                        subscription.renew();
                        subscriptionRepository.save(subscription);
                        
                        // 결제 정보 저장
                        Payment payment = new Payment();
                        payment.setMember(subscription.getSubscriber());
                        payment.setProduct(subscription.getProduct());
                        payment.setMethod(Payment.PaymentMethod.POINT);
                        payment.setAmount(subscription.getMonthlyPrice());
                        payment.setStatus(Payment.PaymentStatus.COMPLETED);
                        payment.setPaymentDate(LocalDateTime.now());
                        payment.setDescription("구독 자동 갱신: " + subscription.getAuthor().getName() + " 작가");
                        payment.setTransactionId("SUB_RENEWAL_" + System.currentTimeMillis());
                        
                        paymentRepository.save(payment);
                        
                        log.info("구독 갱신 완료: 구독자={}, 작가={}", 
                                subscription.getSubscriber().getEmail(), 
                                subscription.getAuthor().getName());
                    }
                } else {
                    // 포인트 부족으로 구독 만료 처리
                    subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
                    subscription.setAutoRenewal(false);
                    subscriptionRepository.save(subscription);
                    
                    log.warn("포인트 부족으로 구독 만료: 구독자={}, 작가={}", 
                            subscription.getSubscriber().getEmail(), 
                            subscription.getAuthor().getName());
                }
            } catch (Exception e) {
                log.error("구독 갱신 처리 중 오류 발생: 구독ID={}, 오류={}", 
                        subscription.getId(), e.getMessage());
            }
        }
        
        log.info("구독 갱신 처리를 완료했습니다. 처리된 구독 수: {}", subscriptionsForRenewal.size());
    }
    
    /**
     * 만료된 구독 처리 (크론잡용)
     */
    public void processExpiredSubscriptions() {
        log.info("만료된 구독 처리를 시작합니다.");
        
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findExpiredSubscriptions(LocalDateTime.now());
        
        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            
            log.info("구독 만료 처리: 구독자={}, 작가={}", 
                    subscription.getSubscriber().getEmail(), 
                    subscription.getAuthor().getName());
        }
        
        log.info("만료된 구독 처리를 완료했습니다. 처리된 구독 수: {}", expiredSubscriptions.size());
    }
    
    /**
     * Response DTO 생성
     */
    private SubscriptionResponseDto createResponseDto(Subscription subscription) {
        SubscriptionResponseDto responseDto = new SubscriptionResponseDto();
        responseDto.setSubscriptionId(subscription.getId());
        responseDto.setProductId(subscription.getProduct().getId());
        responseDto.setProductName(subscription.getProduct().getName());
        responseDto.setAuthorId(subscription.getAuthor().getId());
        responseDto.setAuthorName(subscription.getAuthor().getName());
        responseDto.setStatus(subscription.getStatus().name());
        responseDto.setMonthlyPrice(subscription.getMonthlyPrice());
        responseDto.setStartDate(subscription.getStartDate());
        responseDto.setEndDate(subscription.getEndDate());
        responseDto.setNextBillingDate(subscription.getNextBillingDate());
        responseDto.setAutoRenewal(subscription.getAutoRenewal());
        responseDto.setCancelledAt(subscription.getCancelledAt());
        responseDto.setCancelReason(subscription.getCancelReason());
        
        return responseDto;
    }
}