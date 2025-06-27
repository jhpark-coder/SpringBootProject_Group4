package com.creatorworks.nexus.member.dto;

import com.creatorworks.nexus.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class SessionMemberDto implements Serializable {
    private String email;
    private String name;
    private String gender;
    private String birthYear;
    private String birthMonth;
    private String birthDay;
    private boolean profileComplete;



    public SessionMemberDto(Member member) {
        this.email = member.getEmail();
        this.name = member.getName();
        this.gender = member.getGender();
        this.birthYear = member.getBirthYear();
        this.birthMonth = member.getBirthMonth();
        this.birthDay = member.getBirthDay();
    }
}
