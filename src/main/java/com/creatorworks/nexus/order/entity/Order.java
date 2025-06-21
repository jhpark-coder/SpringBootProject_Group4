package com.creatorworks.nexus.order.entity;

import com.creatorworks.nexus.global.BaseEntity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="order")
public class Order extends BaseEntity {
}
