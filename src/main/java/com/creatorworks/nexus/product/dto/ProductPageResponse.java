package com.creatorworks.nexus.product.dto;

import java.util.List;

import com.creatorworks.nexus.product.entity.Product;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductPageResponse {
  private List<Product> content;
  private int totalPages;
  private long totalElements;
  private int number;
  private int size;
  private boolean last;
  private boolean first;
  private int numberOfElements;
  private boolean empty;

  public ProductPageResponse(org.springframework.data.domain.Page<Product> page) {
    this.content = page.getContent();
    this.totalPages = page.getTotalPages();
    this.totalElements = page.getTotalElements();
    this.number = page.getNumber();
    this.size = page.getSize();
    this.last = page.isLast();
    this.first = page.isFirst();
    this.numberOfElements = page.getNumberOfElements();
    this.empty = page.isEmpty();
  }
} 