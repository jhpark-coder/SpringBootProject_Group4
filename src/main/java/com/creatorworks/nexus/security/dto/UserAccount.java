package com.creatorworks.nexus.security.dto;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.creatorworks.nexus.member.entity.Member;

import lombok.Getter;

@Getter
public class UserAccount extends User implements OAuth2User {

    private final Member member;
    private Map<String, Object> attributes;

    // 일반 로그인용 생성자
    public UserAccount(Member member, Collection<? extends GrantedAuthority> authorities) {
        super(member.getEmail(), member.getPassword(), authorities);
        this.member = member;
    }

    // 소셜 로그인용 생성자
    public UserAccount(Member member, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        super(member.getEmail(), member.getPassword(), authorities);
        this.member = member;
        this.attributes = attributes;
    }
    
    public Long getId() {
        return member.getId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return member.getEmail();
    }
} 