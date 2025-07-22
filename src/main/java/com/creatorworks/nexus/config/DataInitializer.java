package com.creatorworks.nexus.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.repository.AuctionRepository;
import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.product.entity.Point;
import com.creatorworks.nexus.product.entity.Point.PointType;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.PointRepository;
import com.creatorworks.nexus.product.repository.ProductInquiryRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.product.repository.ProductReviewRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@Profile({"dev", "prod"}) // 'dev'와 'prod' 프로필 모두에서 적용됩니다.
@RequiredArgsConstructor
public class DataInitializer {
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductInquiryRepository productInquiryRepository;
    private final PointRepository pointRepository;

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
            }

            // 테스트 유저 계정 생성 (10만 포인트 보유)
            Member testUser = memberRepository.findByEmail("testuser@test.com");
            if (testUser == null) {
                testUser = Member.builder()
                        .email("testuser@test.com")
                        .name("테스트유저")
                        .password(passwordEncoder.encode("password"))
                        .role(Role.USER)
                        .gender("N/A")
                        .birthYear("N/A")
                        .birthMonth("N/A")
                        .birthDay("N/A")
                        // .point(100000) // 삭제: 포인트 필드 사용 안함
                        .build();
                memberRepository.save(testUser);
                // Point 엔티티로 10만 포인트 적립 내역 추가
                Point point = Point.builder()
                        .member(testUser)
                        .amount(100000L)
                        .type(PointType.CHARGE)
                        .balanceAfter(100000L)
                        .description("초기 적립")
                        .impUid(null)
                        .merchantUid(null)
                        .build();
                pointRepository.save(point);
                System.out.println("테스트 유저가 생성되었습니다: testuser@test.com (10만 포인트 적립 내역 포함)");
            }

            // 테스트용 판매자 계정 생성 (기존 작가 계정을 판매자로 변경)
            Member seller;
            if (memberRepository.findByEmail("seller@test.com") == null) {
                seller = Member.builder()
                        .email("seller@test.com")
                        .name("테스트판매자")
                        .password(passwordEncoder.encode("password"))
                        .role(Role.SELLER)
                        .gender("Male")
                        .birthYear("2000")
                        .birthMonth("01")
                        .birthDay("01")
                        .build();
                memberRepository.save(seller);
            } else {
                seller = memberRepository.findByEmail("seller@test.com");
            }

            // 상품 데이터 생성 (필요한 경우)
            if (productRepository.count() == 0) {
                System.out.println("개발 환경: 테스트용 상품 데이터 1000개를 생성합니다.");

                String[] primaryCategories = {"artwork", "graphic-design", "character", "java", "frontend", "python"};
                String[][] secondaryCategories = {
                        {"포토그래피", "일러스트레이션", "스케치", "코믹스"},
                        {"타이포그래피", "앨범아트", "로고", "브랜딩", "편집디자인"},
                        {"카툰", "팬아트", "2D 캐릭터", "3D 모델링"},
                        {"Spring/JPA", "네트워크", "알고리즘", "코어 자바"},
                        {"HTML/CSS", "JavaScript", "React/Vue", "UI/UX"},
                        {"웹 개발", "데이터 분석", "머신러닝", "자동화"}
                };

                // 다양한 상품명 배열
                String[] productNames = {
                        "아름다운 일러스트", "모던한 디자인", "클래식한 아트워크", "미니멀한 그래픽",
                        "컬러풀한 캐릭터", "세련된 로고", "독특한 아이콘", "우아한 타이포그래피",
                        "창의적인 브랜딩", "감성적인 사진", "기하학적 패턴", "유기적 형태",
                        "디지털 아트", "핸드드로잉", "벡터 일러스트", "3D 렌더링",
                        "웹 디자인", "모바일 UI", "앱 아이콘", "배너 디자인",
                        "카드 디자인", "포스터 아트", "앨범 커버", "북 커버",
                        "패키지 디자인", "명함 디자인", "브로셔 디자인", "팜플렛 디자인"
                };

                // 다양한 설명 배열
                String[] descriptions = {
                        "독창적인 아이디어로 제작된 작품입니다.",
                        "세심한 디테일과 완벽한 완성도를 자랑합니다.",
                        "현대적 감각과 전통적 요소가 조화를 이룹니다.",
                        "사용자의 니즈를 고려한 실용적인 디자인입니다.",
                        "감성적이고 아름다운 색감으로 구성되었습니다.",
                        "혁신적인 접근 방식으로 제작된 작품입니다.",
                        "고품질의 재료와 기술로 완성된 작품입니다.",
                        "독특한 스타일과 개성이 돋보이는 작품입니다."
                };

                for (int i = 1; i <= 1000;i++) {
                    // 다양한 상품명 사용
                    String name = productNames[i % productNames.length] + " " + (i / productNames.length + 1);
                    int price = (int) (Math.random() * 90000) + 10000; // 10,000 ~ 99,999원
                    String description = descriptions[i % descriptions.length];

                    // 로컬 static images 폴더의 webp 파일 사용 (1~100 범위)
                    int imageId = (i % 100) + 1; // 1~100 범위로 제한
                    String imageUrl = "/images/" + imageId + ".webp";

                    int categoryIndex = (i - 1) % primaryCategories.length;
                    String pCategory = primaryCategories[categoryIndex];
                    String[] sCategories = secondaryCategories[categoryIndex];
                    String sCategory = sCategories[((i - 1) / primaryCategories.length) % sCategories.length];

                    Product product = Product.builder()
                            .seller(seller)
                            .name(name)
                            .price((long) price)
                            .description(description)
                            .imageUrl(imageUrl)
                            .workDescription("이 작품은 특별한 영감을 받아 제작되었습니다.")
                            .primaryCategory(pCategory)
                            .secondaryCategory(sCategory)
                            .build();

                    // 조회수 설정 (100~10000 사이의 랜덤 값, 일부는 높은 조회수)
                    long viewCount;
                    if (i % 10 == 0) {
                        // 10%는 높은 조회수 (5000~10000)
                        viewCount = (long) (Math.random() * 5000) + 5000;
                    } else {
                        // 90%는 일반적인 조회수 (100~5000)
                        viewCount = (long) (Math.random() * 4900) + 100;
                    }
                    product.setViewCount(viewCount);

                    productRepository.save(product);
                }
                System.out.println("상품 데이터 생성이 완료되었습니다.");

                for (int i = 1; i <= 1000; i++) {
                    // 다양한 상품명 사용
                    String name = productNames[i % productNames.length] + " " + (i / productNames.length + 1);
                    int buyNowPrice = (int) (Math.random() * 90000) + 10000; // 10,000 ~ 99,999원
                    int startBidPrice = buyNowPrice-1000; // 10,000 ~ 99,999원
                    String description = descriptions[i % descriptions.length];

                    // 로컬 static images 폴더의 webp 파일 사용 (1~100 범위)
                    int imageId = (i % 100) + 1; // 1~100 범위로 제한
                    String imageUrl = "/images/" + imageId + ".webp";

                    int categoryIndex = (i - 1) % primaryCategories.length;
                    String pCategory = primaryCategories[categoryIndex];
                    String[] sCategories = secondaryCategories[categoryIndex];
                    String sCategory = sCategories[((i - 1) / primaryCategories.length) % sCategories.length];
                    LocalDateTime now = LocalDateTime.now();
                    int endTimeNum = (int)(Math.random()*10);

                    Auction auction = Auction.builder()
                            .seller(seller)
                            .name(name)
                            .buyNowPrice((long) buyNowPrice)
                            .startBidPrice((long) startBidPrice)
                            .auctionEndTime(now.plusDays(endTimeNum))
                            .description(description)
                            .imageUrl(imageUrl)
                            .workDescription("이 작품은 특별한 영감을 받아 제작되었습니다.")
                            .primaryCategory(pCategory)
                            .secondaryCategory(sCategory)
                            .build();

                    // 조회수 설정 (100~10000 사이의 랜덤 값, 일부는 높은 조회수)
//                    long viewCount;
//                    if (i % 10 == 0) {
//                        // 10%는 높은 조회수 (5000~10000)
//                        viewCount = (long) (Math.random() * 5000) + 5000;
//                    } else {
//                        // 90%는 일반적인 조회수 (100~5000)
//                        viewCount = (long) (Math.random() * 4900) + 100;
//                    }
//                    auction.setViewCount(viewCount);

                    auctionRepository.save(auction);
                }
                System.out.println("경매Auction0 데이터 생성이 완료되었습니다.");
            }

            // 임시 구매 데이터 생성 (usertest가 최근 6개월간 다양한 상품을 구매)
            // 20250701 차트 마이페이지 테스트를 위해 생성
            System.out.println("개발 환경: 차트 테스트를 위한 임시 구매 기록을 생성합니다.");
            if (user != null) {
                if (orderRepository.countByBuyer(user) == 0) {

                    List<Long> productIdsToBuy = List.of(1L, 5L, 10L, 21L, 35L, 50L, 62L);

                    // 기준 날짜를 2025년 6월 15일로 고정!
                    LocalDateTime baseDate = LocalDateTime.of(2025, 6, 15, 10, 30);

                    for (int i = 0; i < productIdsToBuy.size(); i++) {
                        Long productId = productIdsToBuy.get(i);
                        Product productToBuy = productRepository.findById(productId).orElse(null);

                        if (productToBuy != null) {
                            // 기준 날짜로부터 i개월 전으로 주문 날짜를 설정 (모두 2025년 내에 위치하게 됨)
                            int monthsToGoBack = i % 6;
                            LocalDateTime fixedOrderDate = baseDate.minusMonths(monthsToGoBack);

                            Order testOrder = Order.builder()
                                    .buyer(user)
                                    .orderType(Order.OrderType.PRODUCT_PURCHASE)
                                    .orderStatus(Order.OrderStatus.COMPLETED)
                                    .totalAmount(productToBuy.getPrice())
                                    .orderDate(fixedOrderDate) // 고정된 2025년 날짜를 사용
                                    .description("테스트 구매: " + productToBuy.getName())
                                    .product(productToBuy)
                                    .build();

                            orderRepository.save(testOrder);
                        }
                    }
                } else {
                    System.out.println("이미 구매 기록이 존재하므로, 추가 생성하지 않습니다.");
                }
            }

            // ==========================================================
            //      ★★★ 판매자 대시보드 테스트용 데이터 생성 ★★★
            // ==========================================================
            System.out.println("개발 환경: 판매자 대시보드용 테스트 데이터 생성을 시작합니다.");

            // 1. 테스트용 가상 구매자 목록 생성
            List<Member> virtualBuyers = new ArrayList<>();
            // usertest는 제외하고 가상 구매자만 추가
            // (이름, 이메일, 성별, 출생년도) 정보로 가상 구매자 10명 추가 생성
            String[][] buyerInfos = {
                    {"김이십", "buyer1@test.com", "Male", "1998"},
                    {"박삼순", "buyer2@test.com", "Female", "1991"},
                    {"최일구", "buyer3@test.com", "Male", "2005"},
                    {"이삼십", "buyer4@test.com", "Female", "1994"},
                    {"정사오", "buyer5@test.com", "Male", "1988"},
                    {"강이팔", "buyer6@test.com", "Female", "1999"},
                    {"조칠뜨", "buyer7@test.com", "Male", "1977"},
                    {"윤일일", "buyer8@test.com", "Female", "2011"},
                    {"장삼삼", "buyer9@test.com", "Male", "1993"},
                    {"임영영", "buyer10@test.com", "Female", "2001"}
            };

            for (String[] info : buyerInfos) {
                Member buyer = memberRepository.findByEmail(info[1]);
                if (buyer == null) {
                    buyer = Member.builder()
                            .name(info[0])
                            .email(info[1])
                            .password(passwordEncoder.encode("password"))
                            .role(Role.USER)
                            .gender(info[2])
                            .birthYear(info[3])
                            .birthMonth("01").birthDay("01") // 생일은 임의로 통일
                            .build();
                    memberRepository.save(buyer);
                }
                virtualBuyers.add(buyer); // usertest는 포함하지 않음
            }
            System.out.println("가상 구매자 " + virtualBuyers.size() + "명 준비 완료.");

            // 2. 판매자(seller)의 상품 목록 가져오기
            List<Product> sellerProducts = productRepository.findBySeller(seller);

            // 3. 판매자의 상품에 대해 가상 구매 기록 50건 생성
            if (!sellerProducts.isEmpty() && orderRepository.countByProductSeller(seller) < 50) {
                int ordersToCreate = 50;
                for (int i = 0; i < ordersToCreate; i++) {
                    // 무작위 구매자 선택
                    Member randomBuyer = virtualBuyers.get((int) (Math.random() * virtualBuyers.size()));
                    // 무작위 상품 선택
                    Product randomProduct = sellerProducts.get((int) (Math.random() * sellerProducts.size()));
                    // 무작위 구매 날짜 (최근 12개월 이내)
                    LocalDateTime randomOrderDate;
                    // 5건은 "이번 달" 데이터로 강제 생성
                    if (i < 5) {
                        // 이번 달 1일 ~ 오늘 사이의 랜덤한 날짜
                        int dayOfMonth = (int) (Math.random() * LocalDate.now().getDayOfMonth()) + 1;
                        randomOrderDate = LocalDate.now().withDayOfMonth(dayOfMonth).atTime(10, 0);
                    } else {
                        // 나머지는 기존처럼 최근 12개월 내 랜덤 날짜
                        randomOrderDate = LocalDateTime.now().minusDays((long) (Math.random() * 365));
                    }

                    // 중복 구매는 허용한다고 가정하고 바로 생성
                    Order saleRecord = Order.builder()
                            .buyer(randomBuyer)
                            .orderType(Order.OrderType.PRODUCT_PURCHASE)
                            .orderStatus(Order.OrderStatus.COMPLETED)
                            .totalAmount(randomProduct.getPrice())
                            .orderDate(randomOrderDate)
                            .description("테스트 판매: " + randomProduct.getName())
                            .product(randomProduct)
                            .build();
                    orderRepository.save(saleRecord);
                }
                System.out.println("판매자(" + seller.getName() + ")의 상품에 대한 판매 기록 " + ordersToCreate + "건 생성 완료.");
            } else {
                System.out.println("이미 판매자의 판매 기록이 충분하거나 판매할 상품이 없어, 추가 생성하지 않습니다.");
            }
            // ==========================================================
            System.out.println("데이터 초기화 작업이 완료되었습니다.");

            // ====================== 후기 / 문의 테스트 데이터 =====================

            // 후기 100개, 문의 100개만 생성 (이미 있으면 건너뜀)

            if (productReviewRepository.count() == 0 || productInquiryRepository.count() == 0) {

                List<Product> sampleProducts = sellerProducts.subList(0, Math.min(sellerProducts.size(), 20));

                // 1) 후기 생성
                int reviewCount = 0;
                for (Product p : sampleProducts) {
                    Member randomBuyer = virtualBuyers.get((int) (Math.random() * virtualBuyers.size()));
                    int rating = (int) (Math.random() * 5) + 1;
                    String comment = "샘플 후기 " + (++reviewCount) + " - 별점 " + rating;

                    com.creatorworks.nexus.product.entity.ProductReview review = com.creatorworks.nexus.product.entity.ProductReview.builder()
                            .product(p)
                            .writer(randomBuyer)
                            .rating(rating)
                            .comment(comment)
                            .build();
                    productReviewRepository.save(review);
                    if (reviewCount >= 100) break;
                }

                // 2) 문의 생성
                int inquiryCount = 0;
                for (Product p : sampleProducts) {
                    Member randomBuyer = virtualBuyers.get((int) (Math.random() * virtualBuyers.size()));
                    String content = "샘플 문의 " + (++inquiryCount) + " - 이 작품에 대한 질문입니다.";

                    com.creatorworks.nexus.product.entity.ProductInquiry inquiry = com.creatorworks.nexus.product.entity.ProductInquiry.builder()
                            .product(p)
                            .writer(randomBuyer)
                            .content(content)
                            .isSecret(false)
                            .parent(null)
                            .build();
                    productInquiryRepository.save(inquiry);
                    if (inquiryCount >= 100) break;
                }

                System.out.println("샘플 후기 " + reviewCount + "건, 문의 " + inquiryCount + "건 생성 완료");
            }
        };
    }
} 