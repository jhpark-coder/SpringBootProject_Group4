package com.creatorworks.nexus.editor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.creatorworks.nexus.editor.entity.Editor;

@Repository
public interface EditorRepository extends JpaRepository<Editor, Long> {
    List<Editor> findAllByOrderByCreatedAtDesc();
} 