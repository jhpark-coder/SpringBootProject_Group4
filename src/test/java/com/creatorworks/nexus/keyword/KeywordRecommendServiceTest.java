package com.creatorworks.nexus.keyword;

import com.creatorworks.nexus.keyword.dto.KeywordRecommendRequest;
import com.creatorworks.nexus.keyword.dto.KeywordRecommendResponse;
import com.creatorworks.nexus.keyword.service.KeywordRecommendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class KeywordRecommendServiceTest {
    @Autowired
    private KeywordRecommendService keywordRecommendService;

    @Test
    void 키워드_추천_실제_DB_조회_테스트() {
        // given: 임의의 키워드 (DB에 존재할 법한 단어)
        KeywordRecommendRequest request = new KeywordRecommendRequest();
        request.setKeywords(Arrays.asList("아트", "디자인", "작가"));

        // when
        KeywordRecommendResponse response = keywordRecommendService.recommendByKeywords(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getProducts()).isNotNull();
        assertThat(response.getProducts().size()).isLessThanOrEqualTo(3);
        // 결과가 있으면 각 작품의 필수 정보가 채워져 있는지 확인
        response.getProducts().forEach(product -> {
            assertThat(product.getId()).isNotNull();
            assertThat(product.getName()).isNotNull();
        });
    }
} 