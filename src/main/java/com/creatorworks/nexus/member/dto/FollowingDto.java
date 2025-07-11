package com.creatorworks.nexus.member.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowingDto {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime followDate;
} 