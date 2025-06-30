package com.creatorworks.nexus.util.tiptap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * Tiptap JSON 데이터의 최상위 구조에 해당하는 클래스입니다.
 * "type": "doc"과 실제 내용인 "content" 노드 리스트를 담습니다.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipTapDocument {
    private String type;
    private List<TipTapNode> content;
} 