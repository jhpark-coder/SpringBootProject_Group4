package com.creatorworks.nexus.order.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Payment;
import com.creatorworks.nexus.order.entity.Payment.PaymentStatus;
import com.creatorworks.nexus.order.entity.Payment.PaymentType;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Member testMember;
    private Order testOrder;
    private Payment testPayment;
    private Payment testPointPayment;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .build();

        testOrder = Order.builder()
                .buyer(testMember)
                .orderType(Order.OrderType.PRODUCT_PURCHASE)
                .orderStatus(Order.OrderStatus.PENDING)
                .totalAmount(10000L)
                .description("테스트 주문")
                .build();
        testOrder.setId(1L);

        testPayment = Payment.builder()
                .order(testOrder)
                .paymentType(PaymentType.CARD)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(10000L)
                .impUid("imp_test_123")
                .merchantUid("merchant_test_123")
                .customerUid("customer_test_123")
                .cardNumber("1234")
                .cardType("VISA")
                .build();
        testPayment.setId(1L);

        testPointPayment = Payment.builder()
                .order(testOrder)
                .paymentType(PaymentType.POINT)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(10000L)
                .merchantUid("merchant_test_123")
                .build();
        testPointPayment.setId(2L);
    }

    @Test
    void createPayment_성공() {
        // given
        when(paymentRepository.existsByImpUid(anyString())).thenReturn(false);
        when(paymentRepository.existsByMerchantUid(anyString())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // when
        Payment result = paymentService.createPayment(testOrder, PaymentType.CARD, 10000L,
                "imp_test_123", "merchant_test_123", "customer_test_123", "1234", "VISA");

        // then
        assertNotNull(result);
        assertEquals(testOrder, result.getOrder());
        assertEquals(PaymentType.CARD, result.getPaymentType());
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        assertEquals(10000L, result.getAmount());
        assertEquals("imp_test_123", result.getImpUid());
        assertEquals("merchant_test_123", result.getMerchantUid());
        
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_중복impUid_예외발생() {
        // given
        when(paymentRepository.existsByImpUid("imp_test_123")).thenReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(testOrder, PaymentType.CARD, 10000L,
                    "imp_test_123", "merchant_test_123", "customer_test_123", "1234", "VISA");
        });
        
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_중복merchantUid_예외발생() {
        // given
        when(paymentRepository.existsByImpUid(anyString())).thenReturn(false);
        when(paymentRepository.existsByMerchantUid("merchant_test_123")).thenReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(testOrder, PaymentType.CARD, 10000L,
                    "imp_test_123", "merchant_test_123", "customer_test_123", "1234", "VISA");
        });
        
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void completePayment_성공() {
        // given
        when(paymentRepository.findByImpUid("imp_test_123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // when
        Payment result = paymentService.completePayment("imp_test_123");

        // then
        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
        assertNotNull(result.getPaymentDate());
        
        verify(paymentRepository).findByImpUid("imp_test_123");
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void completePayment_결제없음_예외발생() {
        // given
        when(paymentRepository.findByImpUid("imp_test_123")).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.completePayment("imp_test_123");
        });
        
        verify(paymentRepository).findByImpUid("imp_test_123");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void failPayment_성공() {
        // given
        when(paymentRepository.findByImpUid("imp_test_123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // when
        Payment result = paymentService.failPayment("imp_test_123", "카드 잔액 부족");

        // then
        assertNotNull(result);
        assertEquals(PaymentStatus.FAILED, result.getPaymentStatus());
        assertEquals("카드 잔액 부족", result.getFailureReason());
        
        verify(paymentRepository).findByImpUid("imp_test_123");
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void cancelPayment_성공() {
        // given
        when(paymentRepository.findByImpUid("imp_test_123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // when
        Payment result = paymentService.cancelPayment("imp_test_123");

        // then
        assertNotNull(result);
        assertEquals(PaymentStatus.CANCELLED, result.getPaymentStatus());
        
        verify(paymentRepository).findByImpUid("imp_test_123");
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void findByImpUid_성공() {
        // given
        when(paymentRepository.findByImpUid("imp_test_123")).thenReturn(Optional.of(testPayment));

        // when
        Optional<Payment> result = paymentService.findByImpUid("imp_test_123");

        // then
        assertTrue(result.isPresent());
        assertEquals(testPayment, result.get());
        
        verify(paymentRepository).findByImpUid("imp_test_123");
    }

    @Test
    void findByImpUid_결제없음() {
        // given
        when(paymentRepository.findByImpUid("imp_test_123")).thenReturn(Optional.empty());

        // when
        Optional<Payment> result = paymentService.findByImpUid("imp_test_123");

        // then
        assertFalse(result.isPresent());
        
        verify(paymentRepository).findByImpUid("imp_test_123");
    }

    @Test
    void findByMerchantUid_성공() {
        // given
        when(paymentRepository.findByMerchantUid("merchant_test_123")).thenReturn(Optional.of(testPayment));

        // when
        Optional<Payment> result = paymentService.findByMerchantUid("merchant_test_123");

        // then
        assertTrue(result.isPresent());
        assertEquals(testPayment, result.get());
        
        verify(paymentRepository).findByMerchantUid("merchant_test_123");
    }

    @Test
    void findByCustomerUid_성공() {
        // given
        when(paymentRepository.findByCustomerUid("customer_test_123")).thenReturn(Optional.of(testPayment));

        // when
        Optional<Payment> result = paymentService.findByCustomerUid("customer_test_123");

        // then
        assertTrue(result.isPresent());
        assertEquals(testPayment, result.get());
        
        verify(paymentRepository).findByCustomerUid("customer_test_123");
    }

    @Test
    void processPointPayment_성공() {
        // given
        when(paymentRepository.existsByMerchantUid("merchant_test_123")).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPointPayment);

        // when
        Payment result = paymentService.processPointPayment(testOrder, 10000L, "merchant_test_123");

        // then
        assertNotNull(result);
        assertEquals(PaymentType.POINT, result.getPaymentType());
        assertEquals(10000L, result.getAmount());
        assertEquals("merchant_test_123", result.getMerchantUid());
        
        verify(paymentRepository).existsByMerchantUid("merchant_test_123");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processCardPayment_성공() {
        // given
        when(paymentRepository.existsByImpUid(anyString())).thenReturn(false);
        when(paymentRepository.existsByMerchantUid(anyString())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // when
        Payment result = paymentService.processCardPayment(testOrder, 10000L, "imp_test_123", 
                                                         "merchant_test_123", "customer_test_123", "1234", "VISA");

        // then
        assertNotNull(result);
        assertEquals(PaymentType.CARD, result.getPaymentType());
        assertEquals(10000L, result.getAmount());
        assertEquals("imp_test_123", result.getImpUid());
        assertEquals("merchant_test_123", result.getMerchantUid());
        assertEquals("customer_test_123", result.getCustomerUid());
        assertEquals("1234", result.getCardNumber());
        assertEquals("VISA", result.getCardType());
        
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processRecurringPayment_성공() {
        // given
        LocalDateTime nextBillingDate = LocalDateTime.now().plusMonths(1);
        when(paymentRepository.existsByImpUid(anyString())).thenReturn(false);
        when(paymentRepository.existsByMerchantUid(anyString())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // when
        Payment result = paymentService.processRecurringPayment(testOrder, 10000L, "imp_test_123",
                                                              "merchant_test_123", "customer_test_123", "1234", "VISA",
                                                              nextBillingDate);

        // then
        assertNotNull(result);
        assertEquals(PaymentType.CARD, result.getPaymentType());
        assertEquals(10000L, result.getAmount());
        assertEquals(nextBillingDate, result.getNextBillingDate());
        
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void setNextBillingDate_성공() {
        // given
        LocalDateTime nextBillingDate = LocalDateTime.now().plusMonths(1);
        when(paymentRepository.findByCustomerUid("customer_test_123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // when
        paymentService.setNextBillingDate("customer_test_123", nextBillingDate);

        // then
        verify(paymentRepository).findByCustomerUid("customer_test_123");
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void setNextBillingDate_결제없음_예외발생() {
        // given
        LocalDateTime nextBillingDate = LocalDateTime.now().plusMonths(1);
        when(paymentRepository.findByCustomerUid("customer_test_123")).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.setNextBillingDate("customer_test_123", nextBillingDate);
        });
        
        verify(paymentRepository).findByCustomerUid("customer_test_123");
        verify(paymentRepository, never()).save(any(Payment.class));
    }
} 