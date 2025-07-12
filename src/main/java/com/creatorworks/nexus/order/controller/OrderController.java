package com.creatorworks.nexus.order.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Order.OrderType;
import com.creatorworks.nexus.order.entity.Payment;
import com.creatorworks.nexus.order.entity.Payment.PaymentStatus;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.service.OrderService;
import com.creatorworks.nexus.order.service.PaymentService;
import com.creatorworks.nexus.order.service.PointService;
import com.creatorworks.nexus.order.service.RefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PointService pointService;
    private final MemberRepository memberRepository;
    private final RefundService refundService;
    private final OrderRepository orderRepository;

    // === 주문 조회 ===

    /**
     * 내 주문 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<Order>> getMyOrders(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Member member = getMemberFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Order> orders = orderService.getOrdersByBuyer(member, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * 특정 타입 주문 목록 조회
     */
    @GetMapping("/type/{orderType}")
    public ResponseEntity<Page<Order>> getOrdersByType(
            Principal principal,
            @PathVariable String orderType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Member member = getMemberFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size);
        
        try {
            OrderType type = OrderType.valueOf(orderType.toUpperCase());
            Page<Order> orders = orderService.getOrdersByBuyerAndType(member, type, pageable);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderDetail(
            Principal principal,
            @PathVariable Long orderId) {
        
        Member member = getMemberFromPrincipal(principal);
        Optional<Order> orderOpt = orderService.findById(orderId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // 본인의 주문만 조회 가능
            if (order.getBuyer().getId().equals(member.getId())) {
                // 주문 상세 조회 시 isRead를 true로 변경
                if (!order.isRead()) {
                    order.markAsRead();
                    orderRepository.save(order);
                }
                return ResponseEntity.ok(order);
            }
        }
        
        return ResponseEntity.notFound().build();
    }

    // === 포인트 관련 ===

    /**
     * 포인트 충전 (임시: 인증 없이 memberId로도 충전 가능)
     * 테스트 끝나면 아래 임시 코드 삭제하고 기존 주석 해제
     */
    @PostMapping("/points/charge")
    public ResponseEntity<Map<String, Object>> chargePointForTest(
            @RequestBody Map<String, Object> request,
            @RequestParam(required = false) Long memberId,
            Principal principal) {
        System.out.println("==== [LOG] 충전 API 진입 ==== ");
        System.out.println("[LOG] OrderController.chargePointForTest 진입"); // TODO: 테스트 후 삭제
        System.out.println("[LOG] 파라미터: request=" + request + ", memberId=" + memberId + ", principal=" + principal); // TODO: 테스트 후 삭제
        Map<String, Object> response = new HashMap<>();
        try {
            // 기존 principal 기반 코드 주석처리
            // Member member = getMemberFromPrincipal(principal);
            // Long amount = Long.valueOf(request.get("amount").toString());
            // String impUid = (String) request.get("impUid");
            // String merchantUid = (String) request.get("merchantUid");
            // Order order = pointService.chargePoint(member.getId(), amount, impUid, merchantUid);

            // 임시: 인증 없이 memberId로 충전
            Long id = memberId != null ? memberId : (principal != null ? getMemberFromPrincipal(principal).getId() : null);
            System.out.println("[LOG] id 결정: " + id); // TODO: 테스트 후 삭제
            if (id == null) {
                response.put("success", false);
                response.put("message", "memberId 파라미터가 필요합니다.");
                System.out.println("[LOG] memberId 파라미터 없음, 반환: " + response); // TODO: 테스트 후 삭제
                return ResponseEntity.badRequest().body(response);
            }
            Long amount = Long.valueOf(request.get("amount").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            System.out.println("[LOG] amount, paymentMethod: " + amount + ", " + paymentMethod); // TODO: 테스트 후 삭제
            Order order = pointService.chargePoint(id, amount, null, null);
            System.out.println("[LOG] pointService.chargePoint 반환: orderId=" + (order != null ? order.getId() : null)); // TODO: 테스트 후 삭제

            response.put("success", true);
            response.put("orderId", order.getId());
            response.put("message", "포인트 충전 주문이 생성되었습니다.");
            System.out.println("[LOG] 성공 반환: " + response); // TODO: 테스트 후 삭제
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("[ERROR] OrderController.chargePointForTest 예외 발생: " + e.getMessage()); // TODO: 테스트 후 삭제
            e.printStackTrace(); // TODO: 테스트 후 삭제
            response.put("success", false);
            response.put("message", e.getMessage());
            System.out.println("[LOG] 실패 반환: " + response); // TODO: 테스트 후 삭제
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 포인트로 상품 구매
     */
    @PostMapping("/points/purchase")
    public ResponseEntity<Map<String, Object>> purchaseWithPoint(
            @RequestBody Map<String, Object> request,
            Principal principal) {
        
        try {
            Member member = getMemberFromPrincipal(principal);
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            
            Order order = pointService.purchaseWithPoint(member.getId(), productId, quantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.getId());
            response.put("message", "포인트로 상품을 구매했습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 포인트 잔액 조회 (임시: 인증 없이 memberId로도 조회 가능)
     * 테스트 끝나면 아래 임시 코드 삭제하고 기존 주석 해제
     */
    @GetMapping("/points/balance")
    public ResponseEntity<Map<String, Object>> getPointBalanceForTest(
            @RequestParam(required = false) Long memberId,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 기존 principal 기반 코드 주석처리
            // Member member = getMemberFromPrincipal(principal);
            // Long balance = pointService.getCurrentBalance(member.getId());

            // 임시: 인증 없이 memberId로 조회
            if (memberId == null) {
                response.put("success", false);
                response.put("message", "memberId 파라미터가 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            Long balance = pointService.getCurrentBalance(memberId);
            response.put("success", true);
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 포인트 사용 내역 조회
     */
    @GetMapping("/points/history")
    public ResponseEntity<Page<Order>> getPointHistory(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Member member = getMemberFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Order> history = pointService.getPointHistory(member.getId(), pageable);
        return ResponseEntity.ok(history);
    }

    // === 결제 완료 처리 ===

    @GetMapping("/payment/success")
    public String paymentSuccess(
        @RequestParam Long orderId,
        @RequestParam Long amount,
        @RequestParam(required = false) String paymentMethod,
        @RequestParam(required = false) String merchantUid,
        @RequestParam(required = false) String impUid,
        Model model,
        Principal principal
    ) {
        Member member = getMemberFromPrincipal(principal);

        // 결제 정보 조회
        Payment payment = null;
        if (impUid != null) {
            payment = paymentService.findByImpUid(impUid).orElse(null);
        }

        model.addAttribute("amount", amount);
        model.addAttribute("paymentAmount", amount); // 결제 금액
        model.addAttribute("paymentMethod", paymentMethod != null ? paymentMethod : (payment != null ? payment.getPaymentType().name() : "신용카드"));
        model.addAttribute("transactionDate", payment != null && payment.getPaymentDate() != null ? payment.getPaymentDate() : java.time.LocalDateTime.now());
        model.addAttribute("merchantUid", merchantUid);
        model.addAttribute("impUid", impUid);
        model.addAttribute("currentBalance", pointService.getCurrentBalance(member.getId()));
        return "order/pointSuccess";
    }

    /**
     * 포인트로 상품 구매 완료 페이지
     */
    @GetMapping("/points/purchase/success")
    public String pointPurchaseSuccess(
        @RequestParam Long orderId,
        Model model,
        Principal principal
    ) {
        Member member = getMemberFromPrincipal(principal);
        
        // 주문 정보 조회
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || !orderOpt.get().getBuyer().getId().equals(member.getId())) {
            return "redirect:/error/404";
        }
        
        Order order = orderOpt.get();
        
        model.addAttribute("orderId", orderId);
        model.addAttribute("totalAmount", order.getTotalAmount());
        model.addAttribute("orderDate", order.getOrderDate());
        model.addAttribute("currentBalance", pointService.getCurrentBalance(member.getId()));
        
        // 상품 정보가 있는 경우
        if (order.getProduct() != null) {
            model.addAttribute("productName", order.getProduct().getName());
        }
        
        return "order/pointPurchaseSuccess";
    }

    /**
     * 상품 읽음 처리 API
     */
    @PostMapping("/points/products/{productId}/mark-as-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markProductAsRead(
        @PathVariable Long productId,
        Principal principal
    ) {
        try {
            Member member = getMemberFromPrincipal(principal);
            pointService.markProductAsRead(member.getId(), productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상품이 읽음 처리되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 상품 구매 여부 확인 API
     */
    @GetMapping("/points/products/{productId}/purchase-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductPurchaseStatus(
        @PathVariable Long productId,
        Principal principal
    ) {
        try {
            Member member = getMemberFromPrincipal(principal);
            boolean hasPurchased = pointService.hasPurchasedProduct(member.getId(), productId);
            boolean hasRead = pointService.hasReadProduct(member.getId(), productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasPurchased", hasPurchased);
            response.put("hasRead", hasRead);
            response.put("canRefund", hasPurchased && !hasRead);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/payment/fail")
    public String paymentFail(@RequestParam String message, Model model) {
        model.addAttribute("error_msg", message);
        // 실패 시 필요한 다른 정보들도 모델에 추가
        return "order/pointFail";
    }

    /**
     * 결제 완료 처리 (아임포트 웹훅)
     */
    @PostMapping("/payment/complete")
    public ResponseEntity<Map<String, Object>> completePayment(
            @RequestBody Map<String, Object> request,
            Principal principal) {
        try {
            String impUid = (String) request.get("impUid");
            String status = (String) request.get("status");
            Long amount = request.get("amount") != null ? Long.valueOf(request.get("amount").toString()) : null;
            String merchantUid = (String) request.get("merchantUid");
            Long point = request.get("point") != null ? Long.valueOf(request.get("point").toString()) : amount;
            // ... 기타 필요한 값

            if ("paid".equals(status)) {
                Optional<Payment> paymentOpt = paymentService.findByImpUid(impUid);
                Order order = null;
                if (paymentOpt.isEmpty()) {
                    // === 결제 정보가 없으면 주문/결제 정보 새로 생성 ===
                    Member member = getMemberFromPrincipal(principal);
                    order = orderService.createOrder(member, OrderType.POINT_PURCHASE, amount, "포인트 충전", null);
                    paymentService.createPayment(order, Payment.PaymentType.POINT, amount, impUid, merchantUid, null, null, null);
                } else {
                    order = paymentOpt.get().getOrder();
                }

                // 결제 완료 처리(상태 변경 등)
                paymentService.completePayment(impUid);

                // 포인트 적립 등 후처리 (point 값 사용)
                pointService.completePointCharge(impUid, point);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "결제가 완료되었습니다.");
                response.put("orderId", order != null ? order.getId() : null);
                return ResponseEntity.ok(response);
            } else {
                paymentService.failPayment(impUid, "결제 실패: " + status);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "결제에 실패했습니다.");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 결제 상태 확인 API
     */
    @GetMapping("/payment/status")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@RequestParam String impUid) {
        try {
            Optional<Payment> paymentOpt = paymentService.findByImpUid(impUid);
            
            Map<String, Object> response = new HashMap<>();
            
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                response.put("processed", true);
                response.put("success", payment.getPaymentStatus() == PaymentStatus.COMPLETED);
                response.put("status", payment.getPaymentStatus().name());
                response.put("message", payment.getPaymentStatus() == PaymentStatus.COMPLETED ? 
                    "결제가 완료되었습니다." : "결제 처리 중 오류가 발생했습니다.");
            } else {
                response.put("processed", false);
                response.put("success", false);
                response.put("message", "결제 정보를 찾을 수 없습니다.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("success", false);
            response.put("message", "결제 상태 확인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // === 주문 취소 ===

    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long orderId,
            Principal principal) {
        
        try {
            Member member = getMemberFromPrincipal(principal);
            Optional<Order> orderOpt = orderService.findById(orderId);
            
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                // 본인의 주문만 취소 가능
                if (order.getBuyer().getId().equals(member.getId())) {
                    orderService.cancelOrder(orderId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "주문이 취소되었습니다.");
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "주문을 찾을 수 없거나 취소할 권한이 없습니다.");
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // === 페이지 뷰 ===

    /**
     * 주문 목록 페이지
     */
    @GetMapping("/list")
    public String orderListPage(
            Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Member member = getMemberFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Order> orders = orderService.getOrdersByBuyer(member, pageable);

        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(orders.getTotalPages() - 1, page + 2);

        model.addAttribute("orderList", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalElements", orders.getTotalElements());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("Name", member.getName());
        // 주요 통계값 추가
        long totalPurchaseCount = orders.getTotalElements();
        long totalUsedPoint = orders.getContent().stream().mapToLong(Order::getTotalAmount).sum();
        long totalRefundCount = refundService.getMyRefundsCount(member.getId());
        Long totalRefundPoint = refundService.getTotalCompletedRefundAmount(member.getId());
        if (totalRefundPoint == null) totalRefundPoint = 0L;
        model.addAttribute("totalPurchaseCount", totalPurchaseCount);
        model.addAttribute("totalUsedPoint", totalUsedPoint);
        model.addAttribute("totalRefundCount", totalRefundCount);
        model.addAttribute("totalRefundPoint", totalRefundPoint);
        return "order/orderList";
    }

    /**
     * 포인트 관리 페이지
     */
    @GetMapping("/points/charge-page")
    public String pointManagementPage(Model model, Principal principal) {
        try {
            Member member = getMemberFromPrincipal(principal);
            model.addAttribute("memberEmail", member.getEmail());
            model.addAttribute("memberName", member.getName());
            return "order/points";
        } catch (Exception e) {
            // 로그인되지 않은 경우나 기타 오류 발생 시
            model.addAttribute("memberEmail", "");
            model.addAttribute("memberName", "");
            return "order/points";
        }
    }

    // === 헬퍼 메서드 ===

    private Member getMemberFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        String email = principal.getName();
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다: " + email);
        }
        return member;
    }
}
