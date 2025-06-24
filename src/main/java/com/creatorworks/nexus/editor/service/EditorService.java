package com.creatorworks.nexus.editor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.editor.dto.EditorSaveRequest;
import com.creatorworks.nexus.editor.entity.Editor;
import com.creatorworks.nexus.editor.repository.EditorRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EditorService {
    
    private final EditorRepository editorRepository;
    
    public Editor saveEditor(EditorSaveRequest request) {
        Editor editor = Editor.builder()
                .title(request.getTitle())
                .tiptapJson(request.getTiptapJson())
                .htmlBackup(request.getHtmlBackup())
                .coverImage(request.getCoverImage())
                .tags(request.getTags())
                .backgroundColor(request.getBackgroundColor())
                .fontFamily(request.getFontFamily())
                .build();
        
        return editorRepository.save(editor);
    }
    
    @Transactional
    public Editor updateEditor(Long id, EditorSaveRequest request) {
        Editor editor = editorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문서를 찾을 수 없습니다. id=" + id));

        editor.setTitle(request.getTitle());
        editor.setTiptapJson(request.getTiptapJson());
        editor.setHtmlBackup(request.getHtmlBackup());
        editor.setCoverImage(request.getCoverImage());
        editor.setTags(request.getTags());
        editor.setBackgroundColor(request.getBackgroundColor());
        editor.setFontFamily(request.getFontFamily());

        return editor; // Transactional 어노테이션에 의해 변경 감지(dirty checking) 되어 자동 업데이트
    }
    
    @Transactional(readOnly = true)
    public Editor findById(Long id) {
        return editorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Editor not found"));
    }
    
    @Transactional(readOnly = true)
    public List<Editor> findAll() {
        return editorRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Editor getDocument(Long id) {
        return editorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Editor not found"));
    }
} 