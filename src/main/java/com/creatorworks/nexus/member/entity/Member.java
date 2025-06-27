package com.creatorworks.nexus.member.entity;

import com.creatorworks.nexus.global.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name="member")
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nickname;
    private String profileImageUrl;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductHeart> productHearts = new ArrayList<>();

    // 팔로우 관계 (내가 팔로우하는 사람들)
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberFollow> followings = new ArrayList<>();

    // 팔로워 관계 (나를 팔로우하는 사람들)
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberFollow> followers = new ArrayList<>();

    // 팔로우 수 조회 메서드
    public int getFollowerCount() {
        return this.followers != null ? this.followers.size() : 0;
    }

    // 팔로잉 수 조회 메서드
    public int getFollowingCount() {
        return this.followings != null ? this.followings.size() : 0;
    }

}
