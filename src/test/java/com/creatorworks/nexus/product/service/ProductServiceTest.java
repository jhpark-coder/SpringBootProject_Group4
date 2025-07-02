package com.creatorworks.nexus.product.service;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.product.dto.ProductSaveRequest;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductItemTag;
import com.creatorworks.nexus.product.repository.ItemTagRepository;
import com.creatorworks.nexus.product.repository.ProductItemTagRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.product.repository.ProductInquiryRepository;
import com.creatorworks.nexus.product.repository.ProductReviewRepository;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ItemTagRepository itemTagRepository;

    @Autowired
    private ProductItemTagRepository productItemTagRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductInquiryRepository productInquiryRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    private Member testUser;

    @BeforeEach
    void setUp() {
        // 테스트간 데이터 격리를 위해 기존 데이터 삭제 (외래키 제약조건을 고려한 역순으로)
        productItemTagRepository.deleteAllInBatch();
        productInquiryRepository.deleteAllInBatch(); // Product를 참조하므로 먼저 삭제
        productReviewRepository.deleteAllInBatch();  // Product를 참조하므로 먼저 삭제
        orderRepository.deleteAllInBatch();          // Product를 참조하므로 먼저 삭제
        productRepository.deleteAllInBatch();        // 이제 Product 삭제 가능
        memberRepository.deleteAllInBatch();
        itemTagRepository.deleteAllInBatch();

        // 각 테스트 실행 전에 테스트용 사용자 생성 및 저장
        testUser = Member.builder()
                .email("testuser@test.com")
                .name("testUser")
                .password("password")
                .gender("Male")
                .birthYear("2000")
                .birthMonth("01")
                .birthDay("01")
                .role(com.creatorworks.nexus.member.constant.Role.USER)
                .build();
        memberRepository.save(testUser);
    }

    @Test
    @DisplayName("상품을 저장할 때 태그도 함께 저장되어야 한다")
    void saveProductWithTags() {
        // given
        List<String> tags = Arrays.asList("Java", "Spring", "JPA");
        ProductSaveRequest request = new ProductSaveRequest();
        request.setName("테스트 상품");
        request.setPrice(10000L);
        request.setPrimaryCategory("java");
        request.setSecondaryCategory("Spring/JPA");
        request.setTags(tags);

        // when
        Product savedProduct = productService.saveProduct(request, testUser.getEmail());

        // then
        // 1. 상품이 저장되었는지 확인
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
        assertThat(productRepository.findById(savedProduct.getId())).isPresent();

        // 2. 마스터 태그가 3개 생성되었는지 확인
        assertThat(itemTagRepository.findAll()).hasSize(3);
        assertThat(itemTagRepository.findByName("Java")).isPresent();
        assertThat(itemTagRepository.findByName("Spring")).isPresent();
        assertThat(itemTagRepository.findByName("JPA")).isPresent();

        // 3. 상품과 태그의 연결고리가 3개 생성되었는지 확인
        List<ProductItemTag> connections = productItemTagRepository.findAllByProductId(savedProduct.getId());
        assertThat(connections).hasSize(3);
        
        // 4. 연결된 태그 이름이 정확한지 확인
        List<String> savedTagNames = connections.stream()
                .map(connection -> connection.getItemTag().getName())
                .toList();
        assertThat(savedTagNames).containsExactlyInAnyOrder("Java", "Spring", "JPA");
    }
} 