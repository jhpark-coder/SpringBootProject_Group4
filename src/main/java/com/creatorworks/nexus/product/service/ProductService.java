package com.creatorworks.nexus.product.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.creatorworks.nexus.product.dto.ProductSaveRequest;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#pageable")
    public Page<Product> findAllProducts(Pageable pageable) {
        System.out.println("DB에서 상품 목록을 조회합니다. page=" + pageable.getPageNumber());
        return productRepository.findAll(pageable);
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
    }

    public Product saveProduct(ProductSaveRequest request) {
        System.out.println("=== 상품 저장 요청 데이터 ===");
        System.out.println("이름: " + request.getName());
        System.out.println("가격: " + request.getPrice());
        System.out.println("TiptapJson 길이: " + (request.getTiptapJson() != null ? request.getTiptapJson().length() : "null"));
        System.out.println("TiptapJson 내용: " + request.getTiptapJson());
        System.out.println("HtmlBackup 길이: " + (request.getHtmlBackup() != null ? request.getHtmlBackup().length() : "null"));
        System.out.println("배경색: " + request.getBackgroundColor());
        System.out.println("폰트: " + request.getFontFamily());
        System.out.println("===========================");
        
        Product product = new Product();
        product.setName(request.getName());
        product.setImageUrl(request.getImageUrl());
        product.setPrimaryCategory(request.getPrimaryCategory());
        product.setSecondaryCategory(request.getSecondaryCategory());
        product.setDescription(request.getHtmlBackup());
        product.setTiptapJson(request.getTiptapJson());
        product.setBackgroundColor(request.getBackgroundColor());
        product.setFontFamily(request.getFontFamily());
        product.setPrice(request.getPrice());
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductSaveRequest request) {
        System.out.println("Updating product with ID: " + id);
        
        Product product = findProductById(id);
        product.setName(request.getName());
        product.setImageUrl(request.getImageUrl());
        product.setPrimaryCategory(request.getPrimaryCategory());
        product.setSecondaryCategory(request.getSecondaryCategory());
        product.setDescription(request.getHtmlBackup());
        product.setTiptapJson(request.getTiptapJson());
        product.setBackgroundColor(request.getBackgroundColor());
        product.setFontFamily(request.getFontFamily());
        product.setPrice(request.getPrice());
        
        try {
            Product updated = productRepository.save(product);
            System.out.println("Product updated successfully with ID: " + updated.getId());
            return updated;
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
