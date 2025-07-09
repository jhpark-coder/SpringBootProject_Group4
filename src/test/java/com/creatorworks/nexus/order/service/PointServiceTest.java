package com.creatorworks.nexus.order.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Order.OrderStatus;
import com.creatorworks.nexus.order.entity.Order.OrderType;
import com.creatorworks.nexus.order.entity.OrderItem;
import com.creatorworks.nexus.order.entity.Payment;
import com.creatorworks.nexus.order.repository.OrderItemRepository;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.repository.PaymentRepository;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PointService pointService;

    private Member testMember;
    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .point(10000)
                .build();

        testProduct = Product.builder()
                .name("테스트 상품")
                .price(5000L)
                .seller(testMember)
                .description("테스트 상품 설명")
                .build();
        testProduct.setId(1L);

        testOrder = Order.builder()
                .buyer(testMember)
                .orderType(OrderType.POINT_PURCHASE)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(10000L)
                .description("포인트 충전: 10000원")
                .build();
        testOrder.setId(1L);

        testOrderItem = OrderItem.builder()
                .order(testOrder)
                .product(null)
                .author(null)
                .price(10000L)
                .quantity(1)
                .itemType(OrderItem.ItemType.POINT_CHARGE)
                .itemName("포인트 충전")
                .description("포인트 10000원 충전")
                .build();
        testOrderItem.setId(1L);

        testPayment = Payment.builder()
                .order(testOrder)
                .paymentType(Payment.PaymentType.POINT)
                .paymentStatus(Payment.PaymentStatus.PENDING)
                .amount(10000L)
                .merchantUid("merchant_test_123")
                .build();
        testPayment.setId(1L);
    }

    @Test
    void chargePoint_성공() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);
        when(paymentService.processPointPayment(any(Order.class), anyLong(), anyString())).thenReturn(testPayment);

        // when
        Order result = pointService.chargePoint(1L, 10000L, "imp_test_123", "merchant_test_123");

        // then
        assertNotNull(result);
        assertEquals(testMember, result.getBuyer());
        assertEquals(OrderType.POINT_PURCHASE, result.getOrderType());
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        assertEquals(10000L, result.getTotalAmount());
        assertEquals("포인트 충전: 10000원", result.getDescription());
        
        verify(memberRepository).findById(1L);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void chargePoint_회원없음_예외발생() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargePoint(1L, 10000L, null, "merchant_test_123");
        });
        
        verify(memberRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void chargePoint_중복결제_예외발생() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(paymentRepository.existsByImpUid("imp_test_123")).thenReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargePoint(1L, 10000L, "imp_test_123", "merchant_test_123");
        });
        
        verify(memberRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void purchaseWithPoint_성공() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);
        when(paymentService.processPointPayment(any(Order.class), anyLong(), anyString())).thenReturn(testPayment);

        // when
        Order result = pointService.purchaseWithPoint(1L, 1L, 1);

        // then
        assertNotNull(result);
        assertEquals(testMember, result.getBuyer());
        assertEquals(OrderType.PRODUCT_PURCHASE, result.getOrderType());
        assertEquals(OrderStatus.COMPLETED, result.getOrderStatus());
        assertEquals(5000L, result.getTotalAmount());
        assertEquals("포인트로 상품 구매: 테스트 상품", result.getDescription());
        
        verify(memberRepository, times(2)).findById(1L);
        verify(productRepository).findById(1L);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(paymentService).processPointPayment(any(Order.class), eq(5000L), anyString());
    }

    @Test
    void purchaseWithPoint_회원없음_예외발생() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.purchaseWithPoint(1L, 1L, 1);
        });
        
        verify(memberRepository).findById(1L);
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    void purchaseWithPoint_상품없음_예외발생() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.purchaseWithPoint(1L, 1L, 1);
        });
        
        verify(memberRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void purchaseWithPoint_포인트부족_예외발생() {
        // given
        testMember.setPoint(1000); // 부족한 포인트
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.purchaseWithPoint(1L, 1L, 1);
        });
        
        verify(memberRepository, times(2)).findById(1L);
        verify(productRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getCurrentBalance_성공() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // when
        Long result = pointService.getCurrentBalance(1L);

        // then
        assertEquals(10000L, result);
        verify(memberRepository).findById(1L);
    }

    @Test
    void getCurrentBalance_회원없음_예외발생() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.getCurrentBalance(1L);
        });
        
        verify(memberRepository).findById(1L);
    }

    @Test
    void getCurrentBalance_포인트null() {
        // given
        testMember.setPoint(null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // when
        Long result = pointService.getCurrentBalance(1L);

        // then
        assertEquals(0L, result);
        verify(memberRepository).findById(1L);
    }

    @Test
    void getPointHistory_성공() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(orderRepository.findByBuyerAndOrderTypeOrderByOrderDateDesc(any(Member.class), eq(OrderType.POINT_PURCHASE), any())).thenReturn(org.springframework.data.domain.Page.empty());

        // when
        pointService.getPointHistory(1L, org.springframework.data.domain.PageRequest.of(0, 10));

        // then
        verify(memberRepository).findById(1L);
        verify(orderRepository).findByBuyerAndOrderTypeOrderByOrderDateDesc(any(Member.class), eq(OrderType.POINT_PURCHASE), any());
    }

    @Test
    void addPoints_성공() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);
        when(paymentService.processPointPayment(any(Order.class), anyLong(), anyString())).thenReturn(testPayment);

        // when
        Order result = pointService.addPoints(1L, 5000L, "관리자 지급");

        // then
        assertNotNull(result);
        assertEquals(testMember, result.getBuyer());
        assertEquals(OrderType.POINT_PURCHASE, result.getOrderType());
        assertEquals(OrderStatus.COMPLETED, result.getOrderStatus());
        assertEquals(5000L, result.getTotalAmount());
        assertEquals("관리자 지급: 관리자 지급", result.getDescription());
        
        verify(memberRepository, times(2)).findById(1L);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(paymentService).processPointPayment(any(Order.class), eq(5000L), anyString());
    }

    @Test
    void completePointCharge_성공() {
        // given
        testOrder.setPayment(testPayment);
        when(paymentService.findByImpUid("imp_test_123")).thenReturn(Optional.of(testPayment));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // when
        pointService.completePointCharge("imp_test_123", 10000L);

        // then
        verify(paymentService).findByImpUid("imp_test_123");
        verify(paymentService).completePayment("imp_test_123");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void completePointCharge_결제없음() {
        // given
        when(paymentService.findByImpUid("imp_test_123")).thenReturn(Optional.empty());

        // when
        pointService.completePointCharge("imp_test_123", 10000L);

        // then
        verify(paymentService).findByImpUid("imp_test_123");
        verify(paymentService, never()).completePayment(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }
} 