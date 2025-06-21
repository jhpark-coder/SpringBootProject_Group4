package com.creatorworks.nexus.product.entity;

import com.creatorworks.nexus.global.BaseEntity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="product_detail")
public class ProductDetail extends BaseEntity {
}
