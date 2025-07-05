package com.creatorworks.nexus.member.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.dto.OAuthAttributesDto;
import com.creatorworks.nexus.member.dto.SessionMemberDto;
import com.creatorworks.nexus.member.dto.SessionMemberFormDto;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.security.dto.UserAccount;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialMemberService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuthAttributesDto attributes = OAuthAttributesDto.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        Member member = saveOrUpdate(attributes);
        httpSession.setAttribute("member", new SessionMemberFormDto(member));

        // 여기! DefaultOAuth2User 대신 UserAccount를 반환하도록 변경
        return new UserAccount(
                member,
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes.getAttributes()
        );
    }

    private Member saveOrUpdate(OAuthAttributesDto attributes) {
        String email = attributes.getEmail();
        if (email != null) {
            email = email.toLowerCase();
        }
        
        Member member = memberRepository.findByEmail(attributes.getEmail().toLowerCase());
        
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
            if(needsAdditionalInfo(member)) {
                // 정보가 부족하면 저장하지 않고 그냥 반환
                return member;
            }
            // 정보가 충분하면 저장 후 반환
            return memberRepository.save(member);
        } else {
            // 기존 회원 정보 업데이트 (OAuth 정보가 더 최신인 경우)
            if (attributes.getName() != null && (member.getName() == null || member.getName().isEmpty())) {
                member.setName(attributes.getName());
            }
            if (attributes.getGender() != null && (member.getGender() == null || member.getGender().isEmpty())) {
                member.setGender(attributes.getGender());
            }
            if (attributes.getBirthYear() != null && (member.getBirthYear() == null || member.getBirthYear().isEmpty())) {
                member.setBirthYear(attributes.getBirthYear());
            }
            if (attributes.getBirthMonth() != null && (member.getBirthMonth() == null || member.getBirthMonth().isEmpty())) {
                member.setBirthMonth(attributes.getBirthMonth());
            }
            if (attributes.getBirthDay() != null && (member.getBirthDay() == null || member.getBirthDay().isEmpty())) {
                member.setBirthDay(attributes.getBirthDay());
            }
            return memberRepository.save(member);
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
    public void completeSocialSignUp(String email, SessionMemberFormDto formDto) {
        // 1. 이메일로 다시 한번 확인 (그 사이에 다른 경로로 가입했을 경우 대비)
        if (memberRepository.findByEmail(email) != null) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        // 2. OAuth 기본 정보로 Member 객체 생성
        //    이 부분을 위해 OAuthAttributesDto를 DB나 Redis에 임시 저장하거나,
        //    혹은 그냥 필수 필드만으로 새로 만드는 방법도 있습니다.
        //    간단하게는 그냥 formDto로 모든 정보를 받는다고 가정하고 진행하겠습니다.
        Member member = Member.builder() // 또는 new Member()
                .email(email)
                .name(formDto.getName())
                .gender(formDto.getGender())
                .birthYear(formDto.getBirthYear())
                .birthMonth(formDto.getBirthMonth())
                .birthDay(formDto.getBirthDay())
                .role(Role.USER) // 기본 역할 설정
                .password(UUID.randomUUID().toString()) // 소셜 로그인은 비밀번호가 없으므로 임의값 설정
                .build();

        // 3. 드디어 최종 저장!
        memberRepository.save(member);

        // 4. (중요) 저장된 완전한 정보를 바탕으로 새로운 세션 생성
        httpSession.setAttribute("member", new SessionMemberDto(member));
    }
}
