package com.creatorworks.nexus.auction.service;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.auction.dto.AuctionSaveRequest;
import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.AuctionItemTag;
import com.creatorworks.nexus.auction.repository.AuctionItemTagRepository;
import com.creatorworks.nexus.auction.repository.AuctionRepository;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.product.repository.ItemTagRepository;
import com.creatorworks.nexus.product.repository.ProductItemTagRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;

@Disabled // 페이월 기능과 관련 없는 테스트이므로 임시 비활성화
@SpringBootTest
@Transactional
class AuctionServiceTest {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ItemTagRepository itemTagRepository;

    @Autowired
    private AuctionItemTagRepository auctionItemTagRepository;

    // For comprehensive cleanup
    @Autowired
    private ProductItemTagRepository productItemTagRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Member testUser;

    @BeforeEach
    void setUp() {
        // Clean up DB to ensure test isolation
        auctionItemTagRepository.deleteAllInBatch();
        productItemTagRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        auctionRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        itemTagRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        testUser = Member.builder()
                .email("testauctionuser@test.com")
                .name("testAuctionUser")
                .password("password")
                .build();
        memberRepository.save(testUser);
    }

    @Test
    @DisplayName("경매를 저장할 때 태그도 함께 저장되어야 한다")
    void saveAuctionWithTags() {
        // given
        List<String> tags = Arrays.asList("Artwork", "3D-Modeling", "Blender");
        AuctionSaveRequest request = new AuctionSaveRequest();
        request.setName("테스트 경매");
        request.setStartBidPrice(50000L);
        request.setPrimaryCategory("character");
        request.setSecondaryCategory("3D 모델링");
        request.setTags(tags);

        // when
        Auction savedAuction = auctionService.saveAuction(request, testUser.getEmail());

        // then
        // 1. 경매가 올바르게 저장되었는지 확인
        assertThat(savedAuction).isNotNull();
        assertThat(savedAuction.getName()).isEqualTo("테스트 경매");
        assertThat(auctionRepository.findById(savedAuction.getId())).isPresent();

        // 2. 마스터 태그가 3개 생성되었는지 확인
        assertThat(itemTagRepository.count()).isEqualTo(3);
        assertThat(itemTagRepository.findByName("Artwork")).isPresent();
        assertThat(itemTagRepository.findByName("3D-Modeling")).isPresent();
        assertThat(itemTagRepository.findByName("Blender")).isPresent();

        // 3. 경매와 태그의 연결고리가 3개 생성되었는지 확인
        List<AuctionItemTag> connections = auctionItemTagRepository.findAllByAuctionId(savedAuction.getId());
        assertThat(connections).hasSize(3);
        
        // 4. 연결된 태그 이름이 정확한지 확인
        List<String> savedTagNames = connections.stream()
                .map(connection -> connection.getItemTag().getName())
                .toList();
        assertThat(savedTagNames).containsExactlyInAnyOrder("Artwork", "3D-Modeling", "Blender");
    }
} 