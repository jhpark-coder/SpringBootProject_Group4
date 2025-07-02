package com.creatorworks.nexus.faq.entity;

import com.creatorworks.nexus.global.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "faq")
@Getter
@Setter
public class Faq extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private FaqCategory category;
    
    @Column(name = "question", nullable = false, length = 500)
    private String question;
    
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
} 