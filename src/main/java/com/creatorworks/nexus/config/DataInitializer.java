package com.creatorworks.nexus.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@Profile("dev") // 'dev' 프로필이 활성화될 때만 이 설정이 적용됩니다.
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 관리자 계정 생성
            if (memberRepository.findByEmail("admintest@test.com") == null) {
                Member admin = Member.builder()
                        .email("admintest@test.com")
                        .name("어드민")
                        .password(passwordEncoder.encode("password"))
                        .role(Role.ADMIN)
                        .gender("N/A")
                        .birthYear("N/A")
                        .birthMonth("N/A")
                        .birthDay("N/A")
                        .build();
                memberRepository.save(admin);
                System.out.println("초기 데이터: 관리자 계정(admintest@test.com) 생성 완료");
            }

            // 일반 사용자 계정 생성
            if (memberRepository.findByEmail("usertest@test.com") == null) {
                Member user = Member.builder()
                        .email("usertest@test.com")
                        .name("유저")
                        .password(passwordEncoder.encode("password"))
                        .role(Role.USER)
                        .gender("N/A")
                        .birthYear("N/A")
                        .birthMonth("N/A")
                        .birthDay("N/A")
                        .build();
                memberRepository.save(user);
                System.out.println("초기 데이터: 일반 사용자 계정(usertest@test.com) 생성 완료");
            }

            // 기존 작가 계정 생성 로직 (이메일 중복 방지 추가)
            Member author;
            if (memberRepository.findByEmail("author@test.com") == null) {
                author = Member.builder()
                        .email("author@test.com")
                        .name("테스트작가")
                        .password(passwordEncoder.encode("password"))
                        .role(Role.SELLER)
                        .gender("Male")
                        .birthYear("2000")
                        .birthMonth("01")
                        .birthDay("01")
                        .build();
                memberRepository.save(author);
                System.out.println("초기 데이터: 테스트 작가 계정(author@test.com) 생성 완료");
            } else {
                author = memberRepository.findByEmail("author@test.com");
            }

            // 상품 데이터 생성 (필요한 경우)
            if (productRepository.count() == 0) {
                System.out.println("개발 환경: 테스트용 상품 데이터 100개를 생성합니다.");
                for (int i = 1; i <= 100; i++) {
                    String name = "샘플 상품 " + i;
                    int price = (int) (Math.random() * 90000) + 10000; // 10,000 ~ 99,999원
                    String description = "이것은 " + i + "번째 멋진 샘플 상품입니다. 품질이 아주 좋습니다.";
                    // picsum.photos를 사용해 각기 다른 이미지를 보여줍니다.
                    String imageUrl = "https://picsum.photos/id/" + i + "/400/400";

                    Product product = Product.builder()
                            .author(author)
                            .name(name)
                            .price(price)
                            .description(description)
                            .imageUrl(imageUrl)
                            .workDescription("이 작품은 특별한 영감을 받아 제작되었습니다.")
                            .primaryCategory("디지털 아트")
                            .build();

                    productRepository.save(product);
                }
                System.out.println("상품 데이터 생성이 완료되었습니다.");
            }

            System.out.println("데이터 초기화 작업이 완료되었습니다.");
        };
    }
} 