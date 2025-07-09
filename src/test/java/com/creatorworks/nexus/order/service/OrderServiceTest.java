package com.creatorworks.nexus.order.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private PointService pointService;

    @InjectMocks
    private OrderService orderService;

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
                .build();

        testProduct = Product.builder()
                .name("테스트 상품")
                .price(10000L)
                .seller(testMember)
                .build();
        testProduct.setId(1L);

        testOrder = Order.builder()
                .buyer(testMember)
                .orderType(OrderType.PRODUCT_PURCHASE)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(10000L)
                .description("테스트 주문")
                .build();
        testOrder.setId(1L);

        testOrderItem = OrderItem.builder()
                .order(testOrder)
                .product(testProduct)
                .author(testMember)
                .price(10000L)
                .quantity(1)
                .itemType(OrderItem.ItemType.PRODUCT)
                .itemName("테스트 상품")
                .description("테스트 상품 설명")
                .build();
        testOrderItem.setId(1L);

        testPayment = Payment.builder()
                .order(testOrder)
                .paymentType(Payment.PaymentType.CARD)
                .paymentStatus(Payment.PaymentStatus.PENDING)
                .amount(10000L)
                .impUid("imp_test_123")
                .merchantUid("merchant_test_123")
                .build();
        testPayment.setId(1L);
    }

    @Test
    void createOrder_성공() {
        // given
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // when
        Order result = orderService.createOrder(testMember, OrderType.PRODUCT_PURCHASE, 10000L, "테스트 주문");

        // then
        assertNotNull(result);
        assertEquals(testMember, result.getBuyer());
        assertEquals(OrderType.PRODUCT_PURCHASE, result.getOrderType());
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        assertEquals(10000L, result.getTotalAmount());
        assertEquals("테스트 주문", result.getDescription());
        
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void addOrderItem_성공() {
        // given
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);

        // when
        OrderItem result = orderService.addOrderItem(testOrder, testProduct, testMember, 
                                                   10000L, 1, OrderItem.ItemType.PRODUCT,
                                                   "테스트 상품", "테스트 상품 설명");

        // then
        assertNotNull(result);
        assertEquals(testOrder, result.getOrder());
        assertEquals(testProduct, result.getProduct());
        assertEquals(testMember, result.getAuthor());
        assertEquals(10000L, result.getPrice());
        assertEquals(1, result.getQuantity());
        assertEquals(OrderItem.ItemType.PRODUCT, result.getItemType());
        
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void completeOrder_성공() {
        // given
        testOrder.setPayment(testPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // when
        Order result = orderService.completeOrder(1L);

        // then
        assertNotNull(result);
        assertEquals(OrderStatus.COMPLETED, result.getOrderStatus());
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void completeOrder_주문없음_예외발생() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.completeOrder(1L);
        });
        
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_성공() {
        // given
        testOrder.setPayment(testPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // when
        Order result = orderService.cancelOrder(1L);

        // then
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getOrderStatus());
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void findById_성공() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // when
        Optional<Order> result = orderService.findById(1L);

        // then
        assertTrue(result.isPresent());
        assertEquals(testOrder, result.get());
        
        verify(orderRepository).findById(1L);
    }

    @Test
    void findById_주문없음() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        Optional<Order> result = orderService.findById(1L);

        // then
        assertFalse(result.isPresent());
        
        verify(orderRepository).findById(1L);
    }

    @Test
    void findByImpUid_성공() {
        // given
        when(orderRepository.findByPayment_ImpUid("imp_test_123")).thenReturn(Optional.of(testOrder));

        // when
        Optional<Order> result = orderService.findByImpUid("imp_test_123");

        // then
        assertTrue(result.isPresent());
        assertEquals(testOrder, result.get());
        
        verify(orderRepository).findByPayment_ImpUid("imp_test_123");
    }

    @Test
    void findByMerchantUid_성공() {
        // given
        when(orderRepository.findByPayment_MerchantUid("merchant_test_123")).thenReturn(Optional.of(testOrder));

        // when
        Optional<Order> result = orderService.findByMerchantUid("merchant_test_123");

        // then
        assertTrue(result.isPresent());
        assertEquals(testOrder, result.get());
        
        verify(orderRepository).findByPayment_MerchantUid("merchant_test_123");
    }
} 