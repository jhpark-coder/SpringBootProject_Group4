package com.creatorworks.nexus.member.dto;

import com.creatorworks.nexus.member.entity.Member;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FollowingDto {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime followDate;
    
    public FollowingDto(Member member, LocalDateTime followDate) {
        this.id = member.getId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.followDate = followDate;
    }
} 