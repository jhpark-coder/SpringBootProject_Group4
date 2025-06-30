package com.creatorworks.nexus.util.tiptap;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Tiptap JSON 내의 개별 콘텐츠 조각(노드)을 나타내는 클래스입니다.
 * 문단, 제목, 이미지, 텍스트 등 모든 요소를 표현합니다.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipTapNode {
    private String type; // 노드의 종류 (e.g., "paragraph", "heading", "image")
    private List<TipTapNode> content; // 자식 노드 리스트 (재귀 구조)
    private Map<String, Object> attrs; // 속성 (e.g., 이미지의 src, 링크의 href)
    private String text; // 텍스트 노드의 실제 텍스트 내용
    private Integer level; // 제목(heading) 노드의 레벨 (h1, h2, ...)
    private List<TipTapMark> marks; // 텍스트에 적용된 스타일(마크) 리스트
} 