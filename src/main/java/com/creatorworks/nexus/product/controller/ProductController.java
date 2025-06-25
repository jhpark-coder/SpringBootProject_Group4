package com.creatorworks.nexus.product.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.product.dto.ProductSaveRequest;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

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
    public String productDetail() {
        return "product/productDetail";
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
}
