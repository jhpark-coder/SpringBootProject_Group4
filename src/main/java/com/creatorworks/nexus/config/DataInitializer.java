package com.creatorworks.nexus.config;

import com.creatorworks.nexus.product.constant.ProductCategory;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev") // 'dev' 프로필이 활성화될 때만 이 설정이 적용됩니다.
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            System.out.println("개발 환경: 테스트용 상품 데이터 100개를 생성합니다.");
            for (int i = 1; i <= 100; i++) {
                String name = "샘플 상품 " + i;
//                int price = (int) (Math.random() * 90000) + 10000; // 10,000 ~ 99,999원
                String description = "이것은 " + i + "번째 멋진 샘플 상품입니다. 품질이 아주 좋습니다.";
                // picsum.photos를 사용해 각기 다른 이미지를 보여줍니다.
                String imageUrl = "https://picsum.photos/id/" + i + "/400/400";
                ProductCategory category1 = ProductCategory.ARTWORK;
                Product product = new Product(name, description, imageUrl, category1);
                productRepository.save(product);
            }
            System.out.println("데이터 생성이 완료되었습니다.");
        };
    }
} 