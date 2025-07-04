package com.creatorworks.nexus.member.entity;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.dto.MemberFormDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    /**
     * [추가된 메소드]
     * 회원 정보(프로필) 수정을 위한 비즈니스 메소드입니다.
     * 비밀번호를 제외한 프로필 관련 정보만 업데이트합니다.
     */
    public void updateProfile(String name, String gender, String birthYear, String birthMonth, String birthDay) {
        this.name = name;
        this.gender = gender;
        this.birthYear = birthYear;
        this.birthMonth = birthMonth;
        this.birthDay = birthDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Member)) return false;
        Member member = (Member) o;
        if (this.id == null || member.id == null) {
            return false;
        }
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }
    
}
