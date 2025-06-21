package com.creatorworks.nexus.auction.entity;

import com.creatorworks.nexus.global.BaseEntity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="auction")
public class Auction extends BaseEntity {
}
