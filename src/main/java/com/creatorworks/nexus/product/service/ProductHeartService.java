package com.creatorworks.nexus.product.service;

import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductHeart;
import com.creatorworks.nexus.product.repository.ProductHeartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductHeartService {
    private final ProductHeartRepository productHeartRepository;

    public List<Product> getLikedProducts(Long memberId) {
        List<ProductHeart> hearts = productHeartRepository.findByMemberId(memberId);
        return hearts.stream()
                .map(ProductHeart::getProduct)
                .collect(Collectors.toList());
    }
} 