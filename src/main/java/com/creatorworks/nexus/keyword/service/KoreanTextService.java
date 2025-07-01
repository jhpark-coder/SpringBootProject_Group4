package com.creatorworks.nexus.keyword.service;

import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.springframework.stereotype.Service;
import scala.collection.Seq;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class KoreanTextService {
    public List<String> extractNouns(String text) {
        System.out.println("[DEBUG] =================================");
        System.out.println("[DEBUG] extractNouns 호출 시작");
        System.out.println("[DEBUG] 입력 text: '" + text + "'");
        
        try {
            if (text == null || text.trim().isEmpty()) {
                System.out.println("[DEBUG] 입력 텍스트가 null 또는 empty, 빈 리스트 반환");
                return Collections.emptyList();
            }
            
            System.out.println("[DEBUG] 텍스트 정규화 시작...");
            CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
            System.out.println("[DEBUG] 정규화된 텍스트: '" + normalized + "'");
            
            System.out.println("[DEBUG] 토큰화 시작...");
            Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
            List<KoreanTokenizer.KoreanToken> tokenList = scala.collection.JavaConverters.seqAsJavaList(tokens);
            System.out.println("[DEBUG] 총 토큰 개수: " + tokenList.size());
            
            // 모든 토큰 로그 출력 (디버깅용)
            for (int i = 0; i < tokenList.size(); i++) {
                KoreanTokenizer.KoreanToken token = tokenList.get(i);
                System.out.println("[DEBUG] Token[" + i + "]: '" + token.text() + "' (품사: " + token.pos().toString() + ")");
            }
            
            List<String> nouns = tokenList.stream()
                    .filter(token -> {
                        String pos = token.pos().toString();
                        boolean isValidToken = pos.equals("Noun") || 
                                             pos.equals("ProperNoun") || 
                                             pos.equals("Alpha") ||
                                             pos.equals("Foreign");
                        if (isValidToken) {
                            System.out.println("[DEBUG] 유효한 토큰 발견: '" + token.text() + "' (품사: " + pos + ")");
                        }
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
                        System.out.println("[DEBUG] 기술용어 직접 추출: '" + keyword + "'");
                    }
                }
            }
            
            // 간단한 토큰 결합: 연속된 한글 토큰들을 결합
            if (nouns.size() >= 2) {
                List<String> combinedWords = new ArrayList<>();
                StringBuilder currentWord = new StringBuilder();
                
                for (String noun : nouns) {
                    if (noun.length() <= 2 && noun.matches(".*[가-힣].*")) {
                        currentWord.append(noun);
                    } else {
                        if (currentWord.length() > 2) {
                            combinedWords.add(currentWord.toString());
                            System.out.println("[DEBUG] 한글 토큰 결합: '" + currentWord + "'");
                        }
                        currentWord = new StringBuilder();
                    }
                }
                
                // 마지막 결합 단어 처리
                if (currentWord.length() > 2) {
                    combinedWords.add(currentWord.toString());
                    System.out.println("[DEBUG] 한글 토큰 결합: '" + currentWord + "'");
                }
                
                nouns.addAll(combinedWords);
            }
            
            // 중복 제거
            nouns = nouns.stream().distinct().collect(Collectors.toList());
            
            System.out.println("[DEBUG] 추출된 명사들: " + nouns);
            System.out.println("[DEBUG] 추출된 명사 개수: " + nouns.size());
            System.out.println("[DEBUG] extractNouns 완료");
            System.out.println("[DEBUG] =================================");
            
            return nouns;
        } catch (Exception e) {
            System.err.println("[ERROR] =================================");
            System.err.println("[ERROR] 명사 추출 중 오류 발생!");
            System.err.println("[ERROR] 입력값: '" + text + "'");
            System.err.println("[ERROR] 오류 메시지: " + e.getMessage());
            System.err.println("[ERROR] 오류 타입: " + e.getClass().getSimpleName());
            e.printStackTrace();
            System.err.println("[ERROR] =================================");
            return Collections.emptyList();
        }
    }
} 