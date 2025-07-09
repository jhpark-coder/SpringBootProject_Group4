package com.creatorworks.nexus.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberModifyDto {
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;
    @NotEmpty(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;
    @NotBlank(message = "성별은 필수 입력 값입니다.")
    private String gender;        // 성별 (male, female, other)
    @NotBlank(message = "생년은 필수 입력 값입니다.")
    @Pattern(regexp = "^((?!--).)*$", message = "생년을 선택해주세요.")
    private String birthYear;     // 생년
    @NotBlank(message = "생월은 필수 입력 값입니다.")
    @Pattern(regexp = "^((?!--).)*$", message = "생월을 선택해주세요.")
    private String birthMonth;    // 생월
    @NotBlank(message = "생일은 필수 입력 값입니다.")
    @Pattern(regexp = "^((?!--).)*$", message = "생일을 선택해주세요.")
    private String birthDay;      // 생일

} 