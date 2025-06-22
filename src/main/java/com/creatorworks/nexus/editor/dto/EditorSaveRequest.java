package com.creatorworks.nexus.editor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EditorSaveRequest {
    private String title;
    private String tiptapJson;
    private String htmlBackup;
}