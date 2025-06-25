package com.creatorworks.nexus.member.entity;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.dto.MemberFormDto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table(name="member")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String birthYear;

    @Column(nullable = false)
    private String birthMonth;

    @Column(nullable = false)
    private String birthDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public Member update(String name) {
        this.name = name;
        return this;
    }

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setEmail(memberFormDto.getEmail());
        member.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
        member.setName(memberFormDto.getName());
        member.setGender(memberFormDto.getGender());
        member.setBirthYear(memberFormDto.getBirthYear());
        member.setBirthMonth(memberFormDto.getBirthMonth());
        member.setBirthDay(memberFormDto.getBirthDay());
        member.setRole(Role.USER);
        return member;
    }
    
}
