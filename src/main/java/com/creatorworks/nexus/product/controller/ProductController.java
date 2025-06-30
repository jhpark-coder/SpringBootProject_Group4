package com.creatorworks.nexus.product.controller;

import java.util.Map;
import java.util.HashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.dto.ProductSaveRequest;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.service.ProductService;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final MemberRepository memberRepository;

    @PostConstruct
    public void init() {
        System.out.println("=== ProductController 엔드포인트 등록 완료 ===");
        System.out.println("GET  /products/{id} - 상품 상세 페이지");
        System.out.println("GET  /api/products - 상품 목록 API");
        System.out.println("GET  /api/products/{id} - 상품 상세 API");
        System.out.println("POST /api/products/{id}/heart - 좋아요 토글 API");
        System.out.println("GET  /api/products/{id}/heart/status - 좋아요 상태 확인 API");
        System.out.println("GET  /api/products/{id}/heart/count - 좋아요 카운팅 확인 API");
        System.out.println("==========================================");
    }

    // 0. 메인 페이지를 렌더링하는 메서드
    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    // 1. GridView 페이지를 렌더링하는 메서드
    @GetMapping("/grid")
    public String gridView() {
        return "gridView";
    }

    // 1-1. 무한 스크롤 테스트용 GridView 페이지
    @GetMapping("/grid/test")
    public String gridViewTest() {
        return "gridViewTest";
    }

    // 2. 상품 데이터를 JSON으로 반환하는 API 메서드
    // JavaScript가 이 주소로 데이터를 요청합니다.
    @GetMapping("/api/products")
    @ResponseBody
    public Page<Product> getProducts(Pageable pageable) {
        return productService.findAllProducts(pageable);
    }

    // 3. 상품 상세 페이지 렌더링
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model, HttpSession session) {
        try {
            Product product = productService.findProductById(id);
            long heartCount = productService.getHeartCount(id);
            
            // 현재 로그인한 사용자 정보
            Member loginUser = (Member) session.getAttribute("loginUser");
            
            // 좋아요 상태 확인
            boolean isLiked = false;
            if (loginUser != null) {
                isLiked = productService.isLikedByUser(id, loginUser.getUsername());
            }
            
            model.addAttribute("product", product);
            model.addAttribute("heartCount", heartCount);
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("isLiked", isLiked);
            
            System.out.println("상품 상세 페이지 로드 - 상품 ID: " + id + ", 좋아요 수: " + heartCount + ", 좋아요 상태: " + isLiked);
            
            return "product/productDetail";
        } catch (IllegalArgumentException e) {
            System.out.println("상품을 찾을 수 없음 - ID: " + id);
            model.addAttribute("errorMessage", "상품을 찾을 수 없습니다. (ID: " + id + ")");
            model.addAttribute("suggestedIds", "사용 가능한 상품 ID: 1 ~ 10");
            return "error/productNotFound";
        }
    }

    // 4. 특정 상품 데이터를 JSON으로 반환하는 API
    @GetMapping("/api/products/{id}")
    @ResponseBody
    public Product getProduct(@PathVariable("id") Long id) {
        return productService.findProductById(id);
    }

    @PostMapping("/api/products")
    @ResponseBody
    public Long saveProduct(@RequestBody ProductSaveRequest request) {
        Product saved = productService.saveProduct(request);
        return saved.getId();
    }

    @PutMapping("/api/products/{id}")
    @ResponseBody
    public Long updateProduct(@PathVariable Long id, @RequestBody ProductSaveRequest request) {
        Product updated = productService.updateProduct(id, request);
        return updated.getId();
    }

    @GetMapping("/result/product/{id}")
    public String productResult(@PathVariable Long id, Model model) {
        Product product = productService.findProductById(id);
        model.addAttribute("product", product);
        return "product/productResult";
    }

    // 좋아요 토글 API
    @PostMapping("/api/products/{id}/heart")
    @ResponseBody
    public Map<String, Object> toggleHeart(@PathVariable Long id, HttpSession session) {
        System.out.println("=== 좋아요 토글 요청 ===");
        System.out.println("상품 ID: " + id);
        
        Member loginUser = (Member) session.getAttribute("loginUser");
        System.out.println("사용자: " + (loginUser != null ? loginUser.getUsername() : "null"));
        
        if (loginUser == null) {
            System.out.println("오류: 로그인이 필요합니다.");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인이 필요합니다.");
            errorResponse.put("message", "좋아요 기능을 사용하려면 로그인이 필요합니다.");
            return errorResponse;
        }
        
        try {
            boolean isLiked = productService.toggleHeart(id, loginUser.getUsername());
            long heartCount = productService.getHeartCount(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isLiked", isLiked);
            response.put("heartCount", heartCount);
            response.put("message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.");
            
            System.out.println("응답: " + response);
            System.out.println("========================");
            
            return response;
        } catch (Exception e) {
            System.out.println("좋아요 토글 중 오류: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "좋아요 처리 실패");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    // 좋아요 상태 확인 API (테스트용)
    @GetMapping("/api/products/{id}/heart/status")
    @ResponseBody
    public Map<String, Object> getHeartStatus(@PathVariable Long id, HttpSession session) {
        System.out.println("=== 좋아요 상태 확인 요청 ===");
        System.out.println("상품 ID: " + id);
        
        Member loginUser = (Member) session.getAttribute("loginUser");
        System.out.println("사용자: " + (loginUser != null ? loginUser.getUsername() : "null"));
        
        Product product = productService.findProductById(id);
        boolean isLiked = false;
        long heartCount = productService.getHeartCount(id);
        
        if (loginUser != null) {
            try {
                isLiked = productService.isLikedByUser(id, loginUser.getUsername());
            } catch (Exception e) {
                System.out.println("좋아요 상태 확인 중 오류: " + e.getMessage());
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLiked);
        response.put("heartCount", heartCount);
        response.put("productId", id);
        response.put("productName", product.getName());
        response.put("username", loginUser != null ? loginUser.getUsername() : "anonymous");
        
        System.out.println("상태 확인 응답: " + response);
        System.out.println("=============================");
        
        return response;
    }

    // 좋아요 카운팅 확인 API (테스트용)
    @GetMapping("/api/products/{id}/heart/count")
    @ResponseBody
    public Map<String, Object> getHeartCount(@PathVariable Long id) {
        System.out.println("=== 좋아요 카운팅 확인 요청 ===");
        System.out.println("상품 ID: " + id);
        
        try {
            long heartCount = productService.getHeartCount(id);
            Product product = productService.findProductById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("productId", id);
            response.put("productName", product.getName());
            response.put("heartCount", heartCount);
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "success");
            
            System.out.println("카운팅 결과: " + response);
            System.out.println("=============================");
            
            return response;
            
        } catch (Exception e) {
            System.out.println("카운팅 확인 중 오류: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "카운팅 확인 실패");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("productId", id);
            errorResponse.put("status", "error");
            return errorResponse;
        }
    }
    
    // 간단한 테스트용 API
    @GetMapping("/api/test")
    @ResponseBody
    public Map<String, Object> testApi() {
        System.out.println("=== 테스트 API 호출됨 ===");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API가 정상적으로 작동합니다!");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // 테스트용 상품 생성 API
    @PostMapping("/api/test/product")
    @ResponseBody
    public Map<String, Object> createTestProduct() {
        System.out.println("=== 테스트 상품 생성 요청 ===");
        
        try {
            ProductSaveRequest request = new ProductSaveRequest();
            request.setName("테스트 상품");
            request.setPrice(1000);
            request.setDescription("테스트용 상품입니다.");
            request.setImageUrl("https://via.placeholder.com/400x300");
            request.setPrimaryCategory("아트웍");
            request.setSecondaryCategory("디지털아트");
            request.setBackgroundColor("#ffffff");
            request.setFontFamily("Arial");
            request.setTiptapJson("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"테스트 상품 설명\"}]}]}");
            request.setHtmlBackup("<p>테스트 상품 설명</p>");
            
            Product saved = productService.saveProduct(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "테스트 상품이 생성되었습니다!");
            response.put("productId", saved.getId());
            response.put("productName", saved.getName());
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println("테스트 상품 생성 완료: " + response);
            return response;
            
        } catch (Exception e) {
            System.out.println("테스트 상품 생성 실패: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "테스트 상품 생성 실패: " + e.getMessage());
            return response;
        }
    }

    // 테스트용 임시 로그인 API (여러 사용자)
    @PostMapping("/api/test/login/{userId}")
    @ResponseBody
    public Map<String, Object> testLoginWithUser(@PathVariable Long userId, HttpSession session) {
        System.out.println("=== 테스트용 임시 로그인 (사용자 ID: " + userId + ") ===");
        
        try {
            // 기존 사용자 확인
            Member existingUser = memberRepository.findByUsername("testuser" + userId).orElse(null);
            if (existingUser == null) {
                // 새 테스트 사용자 생성
                Member testUser = new Member();
                testUser.setUsername("testuser" + userId);
                testUser.setEmail("test" + userId + "@example.com");
                testUser.setPassword("password123"); // 테스트용 비밀번호
                testUser.setNickname("테스트 사용자 " + userId);
                
                // 데이터베이스에 저장
                existingUser = memberRepository.save(testUser);
                System.out.println("새 테스트 사용자 생성: " + existingUser.getUsername() + " (ID: " + existingUser.getId() + ")");
            } else {
                System.out.println("기존 테스트 사용자 사용: " + existingUser.getUsername() + " (ID: " + existingUser.getId() + ")");
            }
            
            // 세션에 로그인 정보 저장
            session.setAttribute("loginUser", existingUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "테스트용 로그인 완료 (사용자 ID: " + existingUser.getId() + ")");
            response.put("username", existingUser.getUsername());
            response.put("userId", existingUser.getId());
            response.put("nickname", existingUser.getNickname());
            
            System.out.println("테스트 사용자 로그인: " + existingUser.getUsername());
            return response;
            
        } catch (Exception e) {
            System.out.println("테스트 사용자 로그인 실패: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "테스트 사용자 로그인 실패");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    // 테스트용 로그아웃 API
    @PostMapping("/api/test/logout")
    @ResponseBody
    public Map<String, Object> testLogout(HttpSession session) {
        System.out.println("=== 테스트용 로그아웃 ===");
        
        session.removeAttribute("loginUser");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "테스트용 로그아웃 완료");
        
        System.out.println("테스트 사용자 로그아웃");
        return response;
    }

    // 테스트용 로그인 상태 확인 API
    @GetMapping("/api/test/login-status")
    @ResponseBody
    public Map<String, Object> testLoginStatus(HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        
        Map<String, Object> response = new HashMap<>();
        if (loginUser != null) {
            response.put("loggedIn", true);
            response.put("username", loginUser.getUsername());
            response.put("userId", loginUser.getId());
        } else {
            response.put("loggedIn", false);
        }
        
        return response;
    }

    // 테스트용 소셜 기능 페이지
    @GetMapping("/test-social")
    public String testSocialPage() {
        return "test-social";
    }

    // 테스트용 임시 로그인 API (기본)
    @PostMapping("/api/test/login")
    @ResponseBody
    public Map<String, Object> testLogin(HttpSession session) {
        return testLoginWithUser(1L, session);
    }

    // 구독 통계 확인 API
    @GetMapping("/api/test/follow-stats/{creatorId}")
    @ResponseBody
    public Map<String, Object> getFollowStats(@PathVariable Long creatorId) {
        System.out.println("=== 구독 통계 확인 ===");
        System.out.println("작가 ID: " + creatorId);
        
        try {
            // 임시로 Member 객체 생성
            Member creator = new Member();
            creator.setId(creatorId);
            
            // 팔로워 수 계산 (실제로는 MemberFollowService 사용)
            // 여기서는 간단히 데이터베이스에서 직접 조회
            Map<String, Object> response = new HashMap<>();
            response.put("creatorId", creatorId);
            response.put("creatorName", "테스트 작가 " + creatorId);
            response.put("followerCount", 0); // 실제로는 계산 필요
            response.put("followingCount", 0); // 실제로는 계산 필요
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "success");
            
            System.out.println("구독 통계 결과: " + response);
            return response;
            
        } catch (Exception e) {
            System.out.println("구독 통계 확인 중 오류: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "구독 통계 확인 실패");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("creatorId", creatorId);
            errorResponse.put("status", "error");
            return errorResponse;
        }
    }
}
