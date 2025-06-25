package com.creatorworks.nexus.member.dto;

import lombok.Getter;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.entity.Member;

import java.util.HashMap;

@Getter
public class OAuthAttributesDto {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String email;

    @Builder
    public OAuthAttributesDto(Map<String, Object> attributes, String nameAttributeKey, String email) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.email = email;
    }

    // registrationId를 보고 어떤 소셜 로그인인지 판단하여, 각기 다른 JSON 응답을 파싱
    public static OAuthAttributesDto of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        }
        if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    // 구글 생성자
    private static OAuthAttributesDto ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributesDto.builder()
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 카카오 생성자
    private static OAuthAttributesDto ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        // email을 attributes에 직접 추가
        Map<String, Object> newAttributes = new HashMap<>(attributes);
        newAttributes.put("email", kakaoAccount.get("email"));

        return OAuthAttributesDto.builder()
                .email((String) kakaoAccount.get("email"))
                .attributes(newAttributes) // email이 포함된 Map
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 네이버 생성자
    private static OAuthAttributesDto ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributesDto.builder()
                .email((String) response.get("email"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // DTO를 User 엔티티로 변환. 처음 가입할 때 사용.
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .password(UUID.randomUUID().toString())
                .role(Role.USER)
                .build();
    }
}
