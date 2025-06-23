package com.creatorworks.nexus.product.service;

import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

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
}
