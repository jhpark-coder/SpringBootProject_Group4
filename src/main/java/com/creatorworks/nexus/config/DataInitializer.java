package com.creatorworks.nexus.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Bean
    @Profile("dev")
    CommandLineRunner initData() {
        return args -> {
            System.out.println("=== 개발 환경 데이터 초기화 시작 ===");
            
            // 테스트 멤버 생성
            Member testMember = new Member();
            testMember.setUsername("testuser");
            testMember.setEmail("test@example.com");
            testMember.setPassword("password123");
            testMember.setNickname("테스트 사용자");
            memberRepository.save(testMember);
            System.out.println("테스트 멤버 생성: " + testMember.getUsername());
            
            // 테스트 상품들 생성
            String[] categories = {"아트웍", "그래픽디자인", "캐릭터", "Java", "프론트엔드", "Python"};
            String[] subCategories = {"디지털아트", "로고디자인", "일러스트", "웹개발", "앱개발", "데이터분석"};
            
            for (int i = 1; i <= 10; i++) {
                Product product = new Product();
                product.setName("테스트 상품 " + i);
                product.setPrice(1000 + (i * 500));
                product.setDescription("테스트용 상품 " + i + "입니다.");
                product.setImageUrl("https://picsum.photos/400/300?random=" + i);
                product.setPrimaryCategory(categories[(i - 1) % categories.length]);
                product.setSecondaryCategory(subCategories[(i - 1) % subCategories.length]);
                product.setBackgroundColor("#ffffff");
                product.setFontFamily("Arial");
                product.setTiptapJson("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"테스트 상품 " + i + " 설명\"}]}]}");
                
                Product saved = productRepository.save(product);
                System.out.println("테스트 상품 생성: " + saved.getName() + " (ID: " + saved.getId() + ")");
            }
            
            System.out.println("=== 개발 환경 데이터 초기화 완료 ===");
            System.out.println("생성된 상품 ID: 1 ~ 10");
            System.out.println("테스트 멤버: " + testMember.getUsername());
        };
    }
} 