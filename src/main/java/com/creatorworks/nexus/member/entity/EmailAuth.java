package com.creatorworks.nexus.member.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class EmailAuth {
    @Id
    private String email; // 기본 키

    private String authCode; // 인증 코드
    private LocalDateTime expireTime; // 만기 시간
}
