package com.creatorworks.nexus.member.service;

import org.springframework.stereotype.Service;

import com.creatorworks.nexus.member.dto.OAuthAttributesDto;
import com.creatorworks.nexus.member.dto.SessionMemberDto;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialMemberService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행 중인 서비스를 구분하는 코드 (네이버 로그인인지, 구글 로그인인지 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // OAuth2 로그인 진행 시 키가 되는 필드값. (PK와 같은 의미)
        // 이 값은 application.properties의 user-name-attribute에 해당합니다.
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        OAuthAttributesDto attributes = OAuthAttributesDto.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 2. 반환 타입 및 변수명을 Member로 수정합니다.
        Member member = save(attributes);

        // 3. 세션에 저장할 때도 Member 객체를 사용합니다. (SessionMember DTO를 사용한다고 가정)
        httpSession.setAttribute("member", new SessionMemberDto(member));

        // 4. DefaultOAuth2User를 생성할 때도 Member의 정보를 사용합니다.
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes.getAttributes(),
                "email"
        );
    }

    private Member save(OAuthAttributesDto attributes) {
        String email = attributes.getEmail();
        if (email != null) {
            email = email.toLowerCase();
        }
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            
            member = attributes.toEntity();
            member.setEmail(email);
            return memberRepository.save(member);
        } else{
            return member;
        }
    }
}
