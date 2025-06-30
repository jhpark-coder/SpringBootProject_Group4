package com.creatorworks.nexus.util.tiptap;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TipTapRenderer와 관련 DTO들의 기능, 특히 JSON 파싱과 HTML 렌더링을 검증하는 테스트 클래스입니다.
 */
@SpringBootTest
class TipTapRendererTest {

    @Autowired
    private TipTapRenderer tipTapRenderer;

    @Autowired
    private ObjectMapper objectMapper;

    private TipTapDocument testDocument;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트에 사용할 표준 TipTap JSON 데이터입니다. 페이월 노드를 포함하고 있습니다.
        String jsonContent = """
        {
          "type": "doc",
          "content": [
            {
              "type": "heading",
              "attrs": { "level": 1 },
              "content": [ { "type": "text", "text": "공개되는 제목" } ]
            },
            {
              "type": "paragraph",
              "content": [
                { "type": "text", "text": "이 내용은 " },
                { "type": "text", "marks": [{ "type": "bold" }], "text": "모두에게" },
                { "type": "text", "text": " 보입니다." }
              ]
            },
            { "type": "paywall" },
            {
              "type": "paragraph",
              "content": [ { "type": "text", "text": "이 내용은 구매자에게만 보입니다." } ]
            }
          ]
        }
        """;
        testDocument = objectMapper.readValue(jsonContent, TipTapDocument.class);
    }

    @Test
    @DisplayName("JSON 문자열이 TipTapDocument 객체로 올바르게 파싱되는지 테스트")
    void testJsonDeserialization() {
        // Then
        assertNotNull(testDocument, "객체는 null이 아니어야 합니다.");
        assertEquals("doc", testDocument.getType(), "문서 타입은 'doc'이어야 합니다.");
        assertEquals(4, testDocument.getContent().size(), "최상위 노드는 4개여야 합니다.");

        TipTapNode headingNode = testDocument.getContent().get(0);
        assertEquals("heading", headingNode.getType(), "첫 번째 노드는 'heading' 타입이어야 합니다.");
        
        TipTapNode paywallNode = testDocument.getContent().get(2);
        assertEquals("paywall", paywallNode.getType(), "세 번째 노드는 'paywall' 타입이어야 합니다.");
    }

    @Test
    @DisplayName("전체 노드 리스트가 완전한 HTML로 렌더링되는지 테스트 (구매자 시나리오)")
    void testRenderFullContent() {
        // When: 전체 노드 리스트를 렌더링
        String html = tipTapRenderer.render(testDocument.getContent());

        // Then: 페이월 이전과 이후의 내용이 모두 포함되어야 합니다.
        assertTrue(html.contains("<h1>공개되는 제목</h1>"), "h1 태그가 포함되어야 합니다.");
        assertTrue(html.contains("<p>이 내용은 <strong>모두에게</strong> 보입니다.</p>"), "볼드 처리된 공개 단락이 포함되어야 합니다.");
        assertTrue(html.contains("<p>이 내용은 구매자에게만 보입니다.</p>"), "페이월 이후의 비공개 단락이 포함되어야 합니다.");
        assertFalse(html.contains("paywall"), "paywall 이라는 문자열은 렌더링되지 않아야 합니다.");
    }

    @Test
    @DisplayName("페이월 이전 노드 리스트가 부분 HTML로 렌더링되는지 테스트 (미구매자 시나리오)")
    void testRenderPartialContent() {
        // Given: 전체 노드 리스트
        List<TipTapNode> allNodes = testDocument.getContent();

        // When: 'paywall' 노드를 찾아 그 이전까지의 노드만 잘라냅니다.
        int paywallIndex = IntStream.range(0, allNodes.size())
                .filter(i -> "paywall".equals(allNodes.get(i).getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("테스트 데이터에 페이월 노드가 없습니다."));

        List<TipTapNode> partialNodes = allNodes.subList(0, paywallIndex);
        String html = tipTapRenderer.render(partialNodes);

        // Then: 페이월 이전의 내용만 포함되어야 합니다.
        assertTrue(html.contains("<h1>공개되는 제목</h1>"), "h1 태그가 포함되어야 합니다.");
        assertTrue(html.contains("<p>이 내용은 <strong>모두에게</strong> 보입니다.</p>"), "볼드 처리된 공개 단락이 포함되어야 합니다.");
        assertFalse(html.contains("이 내용은 구매자에게만 보입니다."), "페이월 이후의 비공개 단락은 포함되지 않아야 합니다.");
    }
} 