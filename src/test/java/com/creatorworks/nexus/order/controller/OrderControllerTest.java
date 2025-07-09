package com.creatorworks.nexus.order.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Order.OrderStatus;
import com.creatorworks.nexus.order.entity.Order.OrderType;
import com.creatorworks.nexus.order.service.OrderService;
import com.creatorworks.nexus.order.service.PaymentService;
import com.creatorworks.nexus.order.service.PointService;
import com.creatorworks.nexus.security.CustomLoginSuccessHandler;
import com.creatorworks.nexus.security.CustomOAuth2LoginSuccessHandler;
import com.creatorworks.nexus.security.SecurityConfig;
import com.creatorworks.nexus.member.service.SocialMemberService;

@WebMvcTest(controllers = OrderController.class)
@ImportAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class})
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PointService pointService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private SocialMemberService socialMemberService;

    @MockBean
    private CustomOAuth2LoginSuccessHandler customOAuth2LoginSuccessHandler;

    @MockBean
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    @Autowired
    private ObjectMapper objectMapper;

    private Member testMember;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .build();

        testOrder = Order.builder()
                .buyer(testMember)
                .orderType(OrderType.PRODUCT_PURCHASE)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(10000L)
                .description("테스트 주문")
                .build();
        testOrder.setId(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getMyOrders_성공() throws Exception {
        // given
        when(memberRepository.findByEmail("test@example.com")).thenReturn(testMember);
        when(orderService.getOrdersByBuyer(any(Member.class), any())).thenReturn(org.springframework.data.domain.Page.empty());

        // when & then
        mockMvc.perform(get("/api/orders")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrderDetail_성공() throws Exception {
        // given
        when(memberRepository.findByEmail("test@example.com")).thenReturn(testMember);
        when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));

        // when & then
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrderDetail_권한없음() throws Exception {
        // given
        Member otherMember = Member.builder()
                .id(2L)
                .email("other@example.com")
                .name("다른 사용자")
                .build();

        Order otherOrder = Order.builder()
                .buyer(otherMember)
                .orderType(OrderType.PRODUCT_PURCHASE)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(10000L)
                .description("다른 사용자 주문")
                .build();
        otherOrder.setId(1L);

        when(memberRepository.findByEmail("test@example.com")).thenReturn(testMember);
        when(orderService.findById(1L)).thenReturn(Optional.of(otherOrder));

        // when & then
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void chargePointForTest_성공() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 10000L);
        request.put("paymentMethod", "card");

        when(pointService.chargePoint(1L, 10000L, null, null))
                .thenReturn(testOrder);

        // when & then
        mockMvc.perform(post("/api/orders/points/charge")
                .param("memberId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void chargePointForTest_memberId없음_실패() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 10000L);

        // when & then
        mockMvc.perform(post("/api/orders/points/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("memberId 파라미터가 필요합니다."));
    }

    @Test
    void chargePointForTest_예외발생() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("amount", -1000L); // 잘못된 금액

        when(pointService.chargePoint(1L, -1000L, null, null))
                .thenThrow(new IllegalArgumentException("잘못된 금액입니다"));

        // when & then
        mockMvc.perform(post("/api/orders/points/charge")
                .param("memberId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("잘못된 금액입니다"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void purchaseWithPoint_성공() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("productId", 1L);
        request.put("quantity", 1);

        when(memberRepository.findByEmail("test@example.com")).thenReturn(testMember);
        when(pointService.purchaseWithPoint(1L, 1L, 1)).thenReturn(testOrder);

        // when & then
        mockMvc.perform(post("/api/orders/points/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void getPointBalanceForTest_성공() throws Exception {
        // given
        when(pointService.getCurrentBalance(1L)).thenReturn(50000L);

        // when & then
        mockMvc.perform(get("/api/orders/points/balance")
                .param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.balance").value(50000));
    }

    @Test
    void getPointBalanceForTest_memberId없음_실패() throws Exception {
        // when & then
        mockMvc.perform(get("/api/orders/points/balance"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("memberId 파라미터가 필요합니다."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getPointHistory_성공() throws Exception {
        // given
        when(memberRepository.findByEmail("test@example.com")).thenReturn(testMember);
        when(pointService.getPointHistory(1L, any())).thenReturn(org.springframework.data.domain.Page.empty());

        // when & then
        mockMvc.perform(get("/api/orders/points/history")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void completePayment_성공() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("impUid", "imp_test_123");
        request.put("status", "paid");

        when(orderService.findByImpUid("imp_test_123")).thenReturn(Optional.of(testOrder));

        // when & then
        mockMvc.perform(post("/api/orders/payment/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("결제가 완료되었습니다."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void completePayment_실패() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("impUid", "imp_test_123");
        request.put("status", "failed");

        // when & then
        mockMvc.perform(post("/api/orders/payment/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("결제에 실패했습니다."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void cancelOrder_성공() throws Exception {
        // given
        when(memberRepository.findByEmail("test@example.com")).thenReturn(testMember);
        when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderService.cancelOrder(1L)).thenReturn(testOrder);

        // when & then
        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주문이 취소되었습니다"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void cancelOrder_권한없음() throws Exception {
        // given
        Member otherMember = Member.builder()
                .id(2L)
                .email("other@example.com")
                .name("다른 사용자")
                .build();

        Order otherOrder = Order.builder()
                .buyer(otherMember)
                .orderType(OrderType.PRODUCT_PURCHASE)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(10000L)
                .description("다른 사용자 주문")
                .build();
        otherOrder.setId(1L);

        when(memberRepository.findByEmail("test@example.com")).thenReturn(testMember);
        when(orderService.findById(1L)).thenReturn(Optional.of(otherOrder));

        // when & then
        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("주문을 찾을 수 없거나 취소할 권한이 없습니다"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void cancelOrder_주문없음() throws Exception {
        // given
        when(memberRepository.findByEmail("test@example.com")).thenReturn(testMember);
        when(orderService.findById(1L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("주문을 찾을 수 없거나 취소할 권한이 없습니다"));
    }
} 