package com.creatorworks.nexus.keyword.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.springframework.stereotype.Service;

import scala.collection.Seq;

@Service
public class KoreanTextService {
    public List<String> extractNouns(String text) {
        // 한국어 텍스트 명사 추출 시작
        
        try {
            if (text == null || text.trim().isEmpty()) {
                // 입력 텍스트가 null 또는 empty, 빈 리스트 반환
                return Collections.emptyList();
            }
            
            // 텍스트 정규화
            CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
            
            // 토큰화
            Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
            List<KoreanTokenizer.KoreanToken> tokenList = scala.collection.JavaConverters.seqAsJavaList(tokens);
            
            // 모든 토큰 로그 출력 (디버깅용)
            
            List<String> nouns = tokenList.stream()
                    .filter(token -> {
                        String pos = token.pos().toString();
                        boolean isValidToken = pos.equals("Noun") || 
                                             pos.equals("ProperNoun") || 
                                             pos.equals("Alpha") ||
                                             pos.equals("Foreign");
                        // 유효한 토큰 발견
                        return isValidToken;
                    })
                    .map(token -> token.text())
                    .filter(noun -> noun != null && !noun.trim().isEmpty())
                    .collect(Collectors.toList());

            // 추가: 알려진 기술용어들을 원본 텍스트에서 직접 찾기
            List<String> techKeywords = Arrays.asList(
                "노드제이에스", "nodejs", "react", "리액트", "vue", "뷰", 
                "angular", "앵귤러", "django", "디장고", "flask", "플라스크",
                "spring", "스프링", "springboot", "스프링부트", "express", "익스프레스",
                "javascript", "자바스크립트", "typescript", "타입스크립트",
                "python", "파이썬", "java", "자바", "kotlin", "코틀린"
            );
            
            String lowerText = text.toLowerCase();
            for (String keyword : techKeywords) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    if (!nouns.contains(keyword)) {
                        nouns.add(keyword);
                        // 기술용어 직접 추출
                    }
                }
            }
            
            // 중복 제거
            nouns = nouns.stream().distinct().collect(Collectors.toList());
            
            // extractNouns 완료
            
            return nouns;
        } catch (Exception e) {
            // 명사 추출 중 오류 발생
            return Collections.emptyList();
        }
    }
} 