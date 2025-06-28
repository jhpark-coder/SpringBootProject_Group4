package com.creatorworks.nexus.product.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
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
import com.creatorworks.nexus.product.dto.ProductInquiryRequestDto;
import com.creatorworks.nexus.product.dto.ProductPageResponse;
import com.creatorworks.nexus.product.dto.ProductSaveRequest;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductInquiry;
import com.creatorworks.nexus.product.repository.ProductInquiryRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.product.service.ProductInquiryService;
import com.creatorworks.nexus.product.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * @Controller: 이 클래스가 Spring MVC의 컨트롤러임을 나타냅니다.
 *              주로 View(HTML 페이지)를 반환하거나 API 엔드포인트 역할을 합니다.
 * @RequiredArgsConstructor: final 필드에 대한 생성자를 자동으로 생성하여,
 *                           의존성 주입(Dependency Injection)을 간편하게 처리합니다.
 */
@Controller
@RequiredArgsConstructor
public class ProductController {

    // final 키워드와 @RequiredArgsConstructor에 의해, Spring이 자동으로 ProductService의 인스턴스를 주입해줍니다. (생성자 주입)
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductInquiryRepository productInquiryRepository;
    private final ProductInquiryService productInquiryService;
    private final MemberRepository memberRepository;

    /**
     * 그리드 뷰 페이지("/grid") 요청을 처리하여 'gridView.html' 뷰를 렌더링합니다.
     * @return 렌더링할 뷰의 이름 ("gridView")
     */
    @GetMapping("/grid")
    public String gridView() {
        return "gridView";
    }

    /**
     * 무한 스크롤 테스트용 그리드 뷰 페이지("/grid/test")를 렌더링합니다.
     * @return 렌더링할 뷰의 이름 ("gridViewTest")
     */
    @GetMapping("/grid/test")
    public String gridViewTest(Model model, HttpServletRequest request) {
        // CSRF 토큰을 Model에 추가
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "gridViewTest";
    }

    /**
     * 상품 목록 데이터를 JSON 형태로 반환하는 API 엔드포인트입니다. (무한 스크롤 기능에서 사용)
     * @GetMapping("/api/products"): HTTP GET 요청을 이 메소드에 매핑합니다.
     * @ResponseBody: 이 어노테이션은 메소드의 반환값이 뷰 이름이 아니라,
     *                HTTP 응답 본문(Response Body)에 직접 작성되어야 함을 나타냅니다.
     *                객체를 반환하면 Spring이 자동으로 JSON으로 변환해줍니다.
     * @param pageable URL 파라미터(예: ?page=0&size=20)를 통해 전달된 페이징 정보를 담는 객체입니다.
     * @return 페이징 처리된 상품(Product) 목록.
     */
    @GetMapping("/api/products")
    @ResponseBody
    public ProductPageResponse getProducts(Pageable pageable) {
        // ProductService를 통해 페이징 및 DTO 변환이 완료된 결과를 받아 그대로 반환합니다.
        return productService.findAllProducts(pageable);
    }

    /**
     * 특정 상품의 상세 정보 페이지("/products/{id}")를 렌더링합니다.
     * @return 렌더링할 뷰의 이름 ("product/productDetail")
     */
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Long id, 
                                @PageableDefault(size = 4, sort = "regTime", direction = Sort.Direction.DESC) Pageable pageable, 
                                Principal principal,
                                Model model) {
        Product product = productService.findProductById(id);
        Page<ProductInquiry> inquiryPage = productInquiryService.findInquiriesByProduct(id, pageable);

        // 현재 로그인한 사용자 정보 조회
        Member currentMember = null;
        if (principal != null) {
            currentMember = memberRepository.findByEmail(principal.getName());
        }

        model.addAttribute("product", product);
        model.addAttribute("inquiryPage", inquiryPage);
        model.addAttribute("currentMember", currentMember); // 뷰로 전달
        
        return "product/productDetail";
    }

    /**
     * 특정 상품 한 개의 데이터를 JSON 형태로 반환하는 API 엔드포인트입니다.
     * @param id 상품 ID
     * @return 상품 데이터(JSON) 또는 404 Not Found
     */
    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Product> getProductById(@PathVariable("id") Long id) {
        Product product = productService.findProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * 새로운 상품을 저장하는 API 엔드포인트입니다. (웹 에디터에서 사용)
     * @PostMapping("/api/products"): HTTP POST 요청을 처리합니다.
     * @RequestBody: 요청 본문의 JSON 데이터를 ProductSaveRequest 객체로 자동 변환합니다.
     * @param request 상품 저장에 필요한 데이터 (상품명, 에디터 내용 등).
     * @return 저장된 상품의 고유 ID.
     */
    @PostMapping("/api/products")
    @ResponseBody
    public Long saveProduct(@RequestBody ProductSaveRequest request, Principal principal) {
        String userEmail = principal.getName();
        Product saved = productService.saveProduct(request, userEmail);
        return saved.getId();
    }

    /**
     * 기존 상품의 정보를 수정하는 API 엔드포인트입니다. (웹 에디터에서 사용)
     * @PutMapping("/api/products/{id}"): HTTP PUT 요청을 처리하며, 주로 리소스 전체를 수정할 때 사용됩니다.
     * @param id 수정할 상품의 ID.
     * @param request 상품 수정에 필요한 데이터.
     * @return 수정된 상품의 고유 ID.
     */
    @PutMapping("/api/products/{id}")
    @ResponseBody
    public Long updateProduct(@PathVariable Long id, @RequestBody ProductSaveRequest request, Principal principal) {
        // TODO: principal을 사용하여 권한 검증 로직을 서비스 계층에 추가해야 함.
        Product updated = productService.updateProduct(id, request);
        return updated.getId();
    }

    /**
     * 상품 저장 또는 수정 후, 해당 상품의 상세 페이지로 리다이렉트합니다.
     * @param id 결과를 확인할 상품의 ID.
     * @return 상품 상세 페이지로의 리다이렉트 경로.
     */
    @GetMapping("/result/product/{id}")
    public String productResultRedirect(@PathVariable Long id) {
        // 상세 페이지 URL로 리다이렉트
        return "redirect:/products/" + id;
    }

    @PostMapping("/products/{id}/inquiries")
    public String createInquiry(@PathVariable("id") Long productId,
                                ProductInquiryRequestDto inquiryDto,
                                Principal principal) {
        
        String userEmail = principal.getName();
        productInquiryService.createInquiry(productId, inquiryDto, userEmail);
        
        return "redirect:/products/" + productId;
    }

    @PostMapping("/products/{productId}/inquiries/{inquiryId}/replies")
    public String createReply(@PathVariable("productId") Long productId,
                              @PathVariable("inquiryId") Long inquiryId,
                              ProductInquiryRequestDto inquiryDto,
                              Principal principal) {
        
        String userEmail = principal.getName();
        productInquiryService.createReply(productId, inquiryId, inquiryDto, userEmail);

        return "redirect:/products/" + productId;
    }
}
