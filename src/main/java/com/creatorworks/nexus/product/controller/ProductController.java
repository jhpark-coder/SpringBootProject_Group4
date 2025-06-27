package com.creatorworks.nexus.product.controller;

import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping(value = "/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 1. GridView 페이지를 렌더링하는 메서드
    @GetMapping("/grid")
    public String gridView() {
        return "gridView";
    }

    @GetMapping("/infGrid")
    public String prodGrid(
            @RequestParam(value = "category1", required = false) String category1,
            Pageable pageable, // 페이지네이션 정보 자동 바인딩
            Model model) {

        Page<Product> products;
        if (category1 == null || category1.isEmpty()) {
            products = productService.findAllProducts(pageable);
        } else {
            products = productService.findProductsByCategory(category1, pageable);
        }

        model.addAttribute("products", products); // 뷰에 전달할 모델의 이름도 "products"로
        model.addAttribute("category1", category1);

        return "product/productGrid"; // templates/product/list.html 뷰를 반환
    }

    // 1-1. 무한 스크롤 테스트용 GridView 페이지
//    @GetMapping("/grid/test")
//    public String gridViewTest() {
//        return "gridViewTest";
//    }
//
//    // 2. 상품 데이터를 JSON으로 반환하는 API 메서드
//    // JavaScript가 이 주소로 데이터를 요청합니다.
//    @GetMapping("/api/products")
//    @ResponseBody
//    public Page<Product> getProducts(Pageable pageable) {
//        return productService.findAllProducts(pageable);
//    }
//
//    // 3. 상품 상세 페이지 렌더링
//    @GetMapping("/products/{id}")
//    public String productDetail() {
//        return "product/productDetail";
//    }
//
//    // 4. 특정 상품 데이터를 JSON으로 반환하는 API
//    @GetMapping("/api/products/{id}")
//    @ResponseBody
//    public Product getProduct(@PathVariable("id") Long id) {
//        return productService.findProductById(id);
//    }

}
