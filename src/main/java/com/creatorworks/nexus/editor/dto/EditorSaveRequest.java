package com.creatorworks.nexus.editor.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EditorSaveRequest {
    private String title;
    @JsonRawValue
    private String tiptapJson;
    private String htmlBackup;
    private String coverImage;
    private List<String> tags;
    private String backgroundColor;
    private String fontFamily;
} 