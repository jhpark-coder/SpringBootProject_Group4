package com.creatorworks.nexus.point.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "points")
@Getter
@Setter
public class Point extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    @Column(nullable = false)
    private Integer balance = 0; // 기본값 0
    
    @Column(nullable = false)
    private Integer totalEarned = 0; // 총 적립 포인트
    
    @Column(nullable = false)
    private Integer totalUsed = 0; // 총 사용 포인트
} 