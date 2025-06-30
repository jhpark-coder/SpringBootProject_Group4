package com.creatorworks.nexus.member.dto;

import com.creatorworks.nexus.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionMemberDto {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    
    // Member 엔티티로부터 SessionMemberDto를 생성하는 정적 팩토리 메소드
    public static SessionMemberDto from(Member member) {
        return SessionMemberDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
    }
}