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
        Editor editor = new Editor();
        editor.setTitle(request.getTitle());
        editor.setTiptapJson(request.getTiptapJson());
        editor.setHtmlBackup(request.getHtmlBackup());
        
        return editorRepository.save(editor);
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
} 