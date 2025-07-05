package com.creatorworks.nexus.security.dto;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.creatorworks.nexus.member.entity.Member;

import lombok.Getter;

/**
 * OAuth2 사용자 정보를 담는 클래스
 * UserDetails와 OAuth2User를 모두 구현하여 일반 로그인과 소셜 로그인을 모두 지원
 */
@Getter
public class UserAccount implements UserDetails, OAuth2User {
    
    private final Member member;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    
    public UserAccount(Member member, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.member = member;
        this.authorities = authorities;
        this.attributes = attributes;
    }
    
    // UserDetails 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return member.getPassword();
    }
    
    @Override
    public String getUsername() {
        return member.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // OAuth2User 구현
    @Override
    public String getName() {
        return member.getEmail();
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    // 추가 메서드
    public String getEmail() {
        return member.getEmail();
    }
    
    public String getMemberName() {
        return member.getName();
    }
    
    public Long getMemberId() {
        return member.getId();
    }
} 