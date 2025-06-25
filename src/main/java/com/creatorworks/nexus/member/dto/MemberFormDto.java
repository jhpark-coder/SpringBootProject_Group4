package com.creatorworks.nexus.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class MemberFormDto {
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;
    @NotEmpty(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;
    @NotEmpty(message = "비밀번호은 필수 입력 값입니다.")
    @Length(min = 8, max = 16, message = "비밀번호는 8자이상, 16자 이하로 입력해주세요.")
    private String password;
    @NotEmpty(message = "비밀번호를 한번 더 입력하세요.")
    @Length(min = 8, max = 16, message = "비밀번호는 8자이상, 16자 이하로 입니다.")
    private String passwordConfirm;
    @NotEmpty(message = "성별은 필수 입력 값입니다.")
    private String gender;        // 성별 (male, female, other)
    @NotEmpty(message = "생년은 필수 입력 값입니다.")
    private String birthYear;     // 생년
    @NotEmpty(message = "생월은 필수 입력 값입니다.")
    private String birthMonth;    // 생월
    @NotEmpty(message = "생일은 필수 입력 값입니다.")
    private String birthDay;      // 생일
    

}