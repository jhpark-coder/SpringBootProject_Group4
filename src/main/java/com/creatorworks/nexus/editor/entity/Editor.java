package com.creatorworks.nexus.editor.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import com.fasterxml.jackson.annotation.JsonRawValue;

@Entity
@Table(name = "editors")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Editor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @JsonRawValue
    @Column(columnDefinition = "JSON")
    private String tiptapJson;
    
    @Lob
    private String htmlBackup;
    
    @Lob
    private String coverImage;
    
    @ElementCollection
    @CollectionTable(name = "editor_tags", joinColumns = @JoinColumn(name = "editor_id"))
    @Column(name = "tag")
    private List<String> tags;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private EditorStatus status = EditorStatus.DRAFT;
    
    public enum EditorStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
} 