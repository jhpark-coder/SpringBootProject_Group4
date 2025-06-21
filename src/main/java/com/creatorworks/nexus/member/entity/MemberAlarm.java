package com.creatorworks.nexus.member.entity;

import com.creatorworks.nexus.global.BaseEntity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="member_alarm")
public class MemberAlarm extends BaseEntity {
}
