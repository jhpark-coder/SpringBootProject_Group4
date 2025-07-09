package com.creatorworks.nexus.member.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.creatorworks.nexus.member.entity.Member;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Long id;
    private final String username; // email
    private final String password;
    private final String name;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes; // OAuth2 속성

    // 일반 로그인용 생성자
    public CustomUserDetails(Member member) {
        this.id = member.getId();
        this.username = member.getEmail();
        this.password = member.getPassword();
        this.name = member.getName();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().toString()));
    }

    // OAuth2 로그인용 생성자
    public CustomUserDetails(Member member, Map<String, Object> attributes) {
        this.id = member.getId();
        this.username = member.getEmail();
        this.password = member.getPassword();
        this.name = member.getName();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().toString()));
        this.attributes = attributes;
    }

    public Long getId() {
        return id;
    }

    // UserDetails 구현...

    @Override
    public String getName() {
        // OAuth2User의 getName() 구현. attributes에서 이름을 찾아서 반환
        // 여기서는 UserDetails의 name 필드를 그대로 사용해도 무방합니다.
        return name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
    public Map<String, Object> getAttributes() {
        return attributes;
    }
} 