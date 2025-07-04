package com.creatorworks.nexus.admin.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "seller_requests")
public class SellerRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(length = 500)
    private String reason; // 거절 사유

    public enum RequestStatus {
        PENDING("대기중"),
        APPROVED("승인"),
        REJECTED("거절");

        private final String description;

        RequestStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 