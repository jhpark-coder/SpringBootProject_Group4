package com.creatorworks.nexus.auction.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionInquiry extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isSecret = false; // 비밀글 여부, 기본값은 false

    // 부모-자식 관계 (대댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AuctionInquiry parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<AuctionInquiry> children = new ArrayList<>();

    @Builder
    public AuctionInquiry(Auction auction, Member writer, String content, boolean isSecret, AuctionInquiry parent) {
        this.auction = auction;
        this.writer = writer;
        this.content = content;
        this.isSecret = isSecret;
        this.parent = parent;
    }

    //== 연관관계 편의 메서드 ==//
    public void setParent(AuctionInquiry parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }

    //== 비즈니스 로직 ==//
    /**
     * 현재 사용자가 이 문의를 볼 수 있는지 확인합니다.
     * @param currentMember 현재 로그인한 사용자 (비로그인 시 null)
     * @return 조회 가능 여부
     */
    public boolean isViewableBy(Member currentMember) {
        // 1. 비밀글이 아니면 누구나 볼 수 있음
        if (!this.isSecret) {
            return true;
        }

        // 2. 비밀글이지만 로그인하지 않았으면 볼 수 없음
        if (currentMember == null) {
            return false;
        }

        // 3. 비밀글이고, 로그인한 경우
        // 관리자는 모든 비밀글을 볼 수 있음
        if (currentMember.getRole() == Role.ADMIN) {
            return true;
        }

        // 상품 작성자(판매자)는 모든 비밀글을 볼 수 있음
        if (this.auction.getSeller().getId().equals(currentMember.getId())) {
            return true;
        }

        // 문의 작성자 본인만 볼 수 있음
        if (this.writer.getId().equals(currentMember.getId())) {
            return true;
        }

        // 답변글의 경우, 원본 질문 작성자도 볼 수 있음
        if (this.parent != null && this.parent.getWriter().getId().equals(currentMember.getId())) {
            return true;
        }

        return false;
    }
}
