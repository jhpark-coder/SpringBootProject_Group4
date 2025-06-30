package com.creatorworks.nexus.member.entity;

import com.creatorworks.nexus.global.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name="member_follow")
@Entity
public class MemberFollow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팔로우하는 사람 (follower)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private Member follower;

    // 팔로우받는 사람 (following)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private Member following;

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