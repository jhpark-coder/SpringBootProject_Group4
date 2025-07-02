package com.creatorworks.nexus.faq.dto;

import com.creatorworks.nexus.faq.entity.FaqCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FaqDto {
    private Long id;
    
    @JsonProperty("category")
    private FaqCategory category;
    
    @JsonProperty("categoryDisplayName")
    private String categoryDisplayName;
    
    @JsonProperty("question")
    private String question;
    
    @JsonProperty("answer")
    private String answer;
    
    @JsonProperty("sortOrder")
    private Integer sortOrder;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @JsonProperty("viewCount")
    private Integer viewCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
} 