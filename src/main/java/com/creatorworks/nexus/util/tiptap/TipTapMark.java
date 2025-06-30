package com.creatorworks.nexus.util.tiptap;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Tiptap 노드 내의 'marks' 속성을 나타내는 클래스입니다.
 * 'marks'는 텍스트에 적용된 스타일(예: bold, italic, link)을 정의합니다.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipTapMark {
    private String type;
    private Map<String, Object> attrs; // 링크의 href와 같은 추가 속성을 담습니다.
} 