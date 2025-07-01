package com.creatorworks.nexus.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductHeart;
import com.creatorworks.nexus.product.repository.ProductHeartRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@Profile("dev") // 'dev' 프로필이 활성화될 때만 이 설정이 적용됩니다.
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final ProductHeartRepository productHeartRepository;

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
            Member user = memberRepository.findByEmail("usertest@test.com");
            if (user == null) {
                user = Member.builder()
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

                String[] primaryCategories = {"artwork", "graphic-design", "character", "java", "frontend", "python"};
                String[][] secondaryCategories = {
                    {"포토그래피", "일러스트레이션", "스케치", "코믹스"},
                    {"타이포그라피", "앨범아트", "로고", "브랜딩", "편집디자인"},
                    {"카툰", "팬아트", "2D 캐릭터", "3D 모델링"},
                    {"Spring/JPA", "네트워크", "알고리즘", "코어 자바"},
                    {"HTML/CSS", "JavaScript", "React/Vue", "UI/UX"},
                    {"웹 개발", "데이터 분석", "머신러닝", "자동화"}
                };

                // 간단한 키워드 배열
                String[] keywords = {"Java", "Spring", "React", "Python", "디자인", "일러스트", "캐릭터", "웹개발"};

                for (int i = 1; i <= 1000; i++) {
                    // 진행 상황 출력 (매 25개마다)
                    if (i % 25 == 0) {
                        System.out.println("상품 데이터 생성 진행률: " + i + "/100");
                    }

                    int categoryIndex = (i - 1) % primaryCategories.length;
                    String pCategory = primaryCategories[categoryIndex];
                    String[] sCategories = secondaryCategories[categoryIndex];
                    String sCategory = sCategories[((i - 1) / primaryCategories.length) % sCategories.length];

                    String keyword = keywords[i % keywords.length];
                    String name = keyword + " 프로젝트 " + i;
                    int price = (int) (Math.random() * 90000) + 10000;
                    long viewCount = (long) (Math.random() * 10000) + 1;
                    String description = "이것은 " + keyword + "를 활용한 " + i + "번째 프로젝트입니다.";
                    String workDescription = keyword + " 기술을 사용하여 제작된 고품질 작품입니다.";
                    String imageUrl = "https://picsum.photos/id/" + (i % 100) + "/400/400";

                    Product product = Product.builder()
                            .author(author)
                            .name(name)
                            .price((long) price)
                            .description(description)
                            .imageUrl(imageUrl)
                            .workDescription(workDescription)
                            .primaryCategory(pCategory)
                            .secondaryCategory(sCategory)
                            .build();
                    
                    product.setViewCount(viewCount);
                    productRepository.save(product);
                }
                
                System.out.println("상품 데이터 100개 생성이 완료되었습니다!");
                
                // 테스트용 추가 사용자 계정들 생성
                System.out.println("테스트용 좋아요/구매 데이터 생성을 시작합니다...");
                
                Member[] testUsers = new Member[5];
                for (int i = 0; i < 5; i++) {
                    String email = "testuser" + (i+1) + "@test.com";
                    Member testUser = memberRepository.findByEmail(email);
                    if (testUser == null) {
                        testUser = Member.builder()
                                .email(email)
                                .name("테스트유저" + (i+1))
                                .password(passwordEncoder.encode("password"))
                                .role(Role.USER)
                                .gender("N/A")
                                .birthYear("N/A")
                                .birthMonth("N/A")
                                .birthDay("N/A")
                                .build();
                        memberRepository.save(testUser);
                    }
                    testUsers[i] = testUser;
                }
                
                // 모든 Product에 대해 랜덤 좋아요/구매 데이터 생성
                List<Product> allProducts = productRepository.findAll();
                int totalHearts = 0;
                int totalOrders = 0;
                
                for (Product product : allProducts) {
                    // 랜덤 좋아요 생성 (0~4명이 좋아요)
                    int heartCount = (int)(Math.random() * 5);
                    for (int i = 0; i < heartCount; i++) {
                        Member randomUser = testUsers[(int)(Math.random() * testUsers.length)];
                        
                        // 중복 좋아요 방지 체크
                        if (productHeartRepository.findByMemberIdAndProductId(randomUser.getId(), product.getId()).isEmpty()) {
                            ProductHeart heart = new ProductHeart();
                            heart.setMember(randomUser);
                            heart.setProduct(product);
                            productHeartRepository.save(heart);
                            totalHearts++;
                        }
                    }
                    
                    // 랜덤 구매 생성 (0~2명이 구매)
                    int purchaseCount = (int)(Math.random() * 3);
                    for (int i = 0; i < purchaseCount; i++) {
                        Member randomUser = testUsers[(int)(Math.random() * testUsers.length)];
                        
                        // 중복 구매 방지 체크
                        if (!orderRepository.existsByBuyerAndProduct(randomUser, product)) {
                            Order order = Order.builder()
                                    .buyer(randomUser)
                                    .product(product)
                                    .build();
                            orderRepository.save(order);
                            totalOrders++;
                        }
                    }
                }
                
                System.out.println("🎉 테스트 데이터 생성 완료!");
                System.out.println("📊 생성된 좋아요 수: " + totalHearts + "개");
                System.out.println("📊 생성된 구매 수: " + totalOrders + "개");
                
                // usertest@test.com이 상품1을 구매하도록 보장
                Product product1 = productRepository.findById(1L).orElse(null);
                if (product1 != null && !orderRepository.existsByBuyerAndProduct(user, product1)) {
                    Order userTestOrder = Order.builder()
                            .buyer(user)
                            .product(product1)
                            .build();
                    orderRepository.save(userTestOrder);
                    System.out.println("🛒 usertest@test.com이 상품1번을 구매하도록 설정했습니다!");
                    totalOrders++;
                }
                
                System.out.println("📊 최종 구매 수: " + totalOrders + "개");
                System.out.println("📊 이제 키워드 검색에서 정렬 테스트가 가능합니다!");
            }

            System.out.println("데이터 초기화 작업이 완료되었습니다.");
        };
    }
} 