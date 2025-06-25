package com.creatorworks.nexus.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailAuthRequestDto {
    private String email;
    private String authCode;
}
