package com.creatorworks.nexus.member.entity;

import com.creatorworks.nexus.global.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member_follow")
public class MemberFollow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 구독을 하는 사용자 (외래 키: follower_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private Member follower;

    // 구독을 받는 사용자 (외래 키: following_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private Member following;

    @Builder
    public MemberFollow(Member follower, Member following) {
        this.follower = follower;
        this.following = following;
    }

    // 팔로우 관계의 고유성을 보장하기 위한 equals, hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MemberFollow that = (MemberFollow) obj;
        return follower.getId().equals(that.follower.getId()) &&
               following.getId().equals(that.following.getId());
    }

    @Override
    public int hashCode() {
        return follower.getId().hashCode() * 31 + following.getId().hashCode();
    }
}
