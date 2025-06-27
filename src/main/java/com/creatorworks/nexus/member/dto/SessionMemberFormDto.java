package com.creatorworks.nexus.member.dto;

import java.io.Serializable;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionMemberFormDto implements Serializable{

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;
    @NotEmpty(message = "성별은 필수 입력 값입니다.")
    private String gender;        // 성별 (male, female, other)
    @NotEmpty(message = "생년은 필수 입력 값입니다.")
    private String birthYear;     // 생년
    @NotEmpty(message = "생월은 필수 입력 값입니다.")
    private String birthMonth;    // 생월
    @NotEmpty(message = "생일은 필수 입력 값입니다.")
    private String birthDay;      // 생일
    
}
