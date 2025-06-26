package com.creatorworks.nexus.member.service;

import org.springframework.stereotype.Service;
import com.creatorworks.nexus.member.dto.OAuthAttributesDto;
import com.creatorworks.nexus.member.dto.SessionMemberDto;
import com.creatorworks.nexus.member.dto.SessionMemberFormDto;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
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

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        
        OAuthAttributesDto attributes = OAuthAttributesDto.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // OAuth 정보로 Member 생성 또는 업데이트
        Member member = saveOrUpdate(attributes);

        // 세션에 저장
        httpSession.setAttribute("member", new SessionMemberDto(member));
        
        // 추가 정보 입력이 필요한지 확인하여 세션에 플래그 저장
        boolean needsAdditionalInfo = needsAdditionalInfo(member);
        httpSession.setAttribute("needsAdditionalInfo", needsAdditionalInfo);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes.getAttributes(),
                "email"
        );
    }

    private Member saveOrUpdate(OAuthAttributesDto attributes) {
        String email = attributes.getEmail();
        if (email != null) {
            email = email.toLowerCase();
        }
        
        Member member = memberRepository.findByEmail(email);
        
        if (member == null) {
            // 새 회원 생성
            member = attributes.toEntity();
            member.setEmail(email);
            
            // OAuth에서 제공한 정보가 있으면 설정
            if (attributes.getName() != null) {
                member.setName(attributes.getName());
            }
            if (attributes.getGender() != null) {
                member.setGender(attributes.getGender());
            }
            if (attributes.getBirthYear() != null) {
                member.setBirthYear(attributes.getBirthYear());
            }
            if (attributes.getBirthMonth() != null) {
                member.setBirthMonth(attributes.getBirthMonth());
            }
            if (attributes.getBirthDay() != null) {
                member.setBirthDay(attributes.getBirthDay());
            }
            
            return memberRepository.save(member);
        } else {
            // 기존 회원 정보 업데이트 (OAuth 정보가 더 최신인 경우)
            boolean updated = false;
            
            if (attributes.getName() != null && (member.getName() == null || member.getName().isEmpty())) {
                member.setName(attributes.getName());
                updated = true;
            }
            if (attributes.getGender() != null && (member.getGender() == null || member.getGender().isEmpty())) {
                member.setGender(attributes.getGender());
                updated = true;
            }
            if (attributes.getBirthYear() != null && (member.getBirthYear() == null || member.getBirthYear().isEmpty())) {
                member.setBirthYear(attributes.getBirthYear());
                updated = true;
            }
            if (attributes.getBirthMonth() != null && (member.getBirthMonth() == null || member.getBirthMonth().isEmpty())) {
                member.setBirthMonth(attributes.getBirthMonth());
                updated = true;
            }
            if (attributes.getBirthDay() != null && (member.getBirthDay() == null || member.getBirthDay().isEmpty())) {
                member.setBirthDay(attributes.getBirthDay());
                updated = true;
            }
            
            if (updated) {
                return memberRepository.save(member);
            }
            
            return member;
        }
    }

    // 추가 정보 입력이 필요한지 확인하는 메서드
    private boolean needsAdditionalInfo(Member member) {
        // 필수 정보가 모두 있는지 확인
        return member.getName() == null || member.getName().isEmpty() ||
               member.getGender() == null || member.getGender().isEmpty() ||
               member.getBirthYear() == null || member.getBirthYear().isEmpty() ||
               member.getBirthMonth() == null || member.getBirthMonth().isEmpty() ||
               member.getBirthDay() == null || member.getBirthDay().isEmpty();
    }

    // 추가 정보 업데이트 메서드
    public void updateSocialMemberInfo(String email, SessionMemberFormDto formDto) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalStateException("회원을 찾을 수 없습니다.");
        }
        
        // 기존 정보가 없는 경우에만 업데이트
        if (member.getName() == null || member.getName().isEmpty()) {
            member.setName(formDto.getName());
        }
        if (member.getGender() == null || member.getGender().isEmpty()) {
            member.setGender(formDto.getGender());
        }
        if (member.getBirthYear() == null || member.getBirthYear().isEmpty()) {
            member.setBirthYear(formDto.getBirthYear());
        }
        if (member.getBirthMonth() == null || member.getBirthMonth().isEmpty()) {
            member.setBirthMonth(formDto.getBirthMonth());
        }
        if (member.getBirthDay() == null || member.getBirthDay().isEmpty()) {
            member.setBirthDay(formDto.getBirthDay());
        }
        
        memberRepository.save(member);
    }
}
