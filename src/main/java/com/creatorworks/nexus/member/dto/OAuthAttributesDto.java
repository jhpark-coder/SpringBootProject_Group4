package com.creatorworks.nexus.member.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthAttributesDto {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String email;
    private final String name;
    private final String gender;
    private final String birthYear;
    private final String birthMonth;
    private final String birthDay;

    @Builder
    public OAuthAttributesDto(Map<String, Object> attributes, String nameAttributeKey, String email, 
                             String name, String gender, String birthYear, String birthMonth, String birthDay) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.birthYear = birthYear;
        this.birthMonth = birthMonth;
        this.birthDay = birthDay;
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
                .name((String) attributes.get("name"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 카카오 생성자
    @SuppressWarnings("unchecked")
    private static OAuthAttributesDto ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        // email을 attributes에 직접 추가
        Map<String, Object> newAttributes = new HashMap<>(attributes);
        newAttributes.put("email", kakaoAccount.get("email"));

        return OAuthAttributesDto.builder()
                .email((String) kakaoAccount.get("email"))
                .name((String) profile.get("nickname"))
                .attributes(newAttributes) // email이 포함된 Map
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 네이버 생성자
    @SuppressWarnings("unchecked")
    private static OAuthAttributesDto ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributesDto.builder()
                .email((String) response.get("email"))
                .name((String) response.get("name"))
                .gender((String) response.get("gender"))
                .birthYear((String) response.get("birthyear"))
                .birthMonth((String) response.get("birthday").toString().substring(0, 2))
                .birthDay((String) response.get("birthday").toString().substring(2, 4))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // DTO를 User 엔티티로 변환. 처음 가입할 때 사용.
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .password(UUID.randomUUID().toString())
                .name(name)
                .gender(gender)
                .birthYear(birthYear)
                .birthMonth(birthMonth)
                .birthDay(birthDay)
                .role(Role.USER)
                .build();
    }
}
