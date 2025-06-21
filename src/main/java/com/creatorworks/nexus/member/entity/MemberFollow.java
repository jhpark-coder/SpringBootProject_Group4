package com.creatorworks.nexus.member.entity;

import com.creatorworks.nexus.global.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="member_follow")
@Entity
public class MemberFollow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
