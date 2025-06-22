package com.creatorworks.nexus.editor.controller;

import java.io.File;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.creatorworks.nexus.editor.dto.EditorSaveRequest;
import com.creatorworks.nexus.editor.entity.Editor;
import com.creatorworks.nexus.editor.service.EditorService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/editor")
public class EditorController {
    
    private final EditorService editorService;
    
    // 에디터 페이지
    @GetMapping
    public String editor() {
        return "editor/editor";
    }
    
    // 에디터 저장 API
    @PostMapping("/api/documents")
    @ResponseBody
    public ResponseEntity<Long> saveEditor(@RequestBody EditorSaveRequest request) {
        Editor savedEditor = editorService.saveEditor(request);
        return ResponseEntity.ok(savedEditor.getId());
    }
    
    // 저장된 에디터 결과 페이지
    @GetMapping("/result/{id}")
    public String resultPage(@PathVariable Long id, Model model) {
        Editor editor = editorService.findById(id);
        model.addAttribute("editor", editor);
        return "editor/result";
    }
    
    // 에디터 목록 페이지
    @GetMapping("/list")
    public String listPage(Model model) {
        List<Editor> editors = editorService.findAll();
        model.addAttribute("editors", editors);
        return "editor/list";
    }
    
    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("파일 업로드 요청 받음: " + (file != null ? file.getOriginalFilename() : "null"));
            
            if (file == null) {
                return ResponseEntity.badRequest().body("파일이 null입니다.");
            }
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어있습니다.");
            }
            
            // 1. 파일 타입별 폴더 결정
            String fileType = determineFileType(file);
            String baseUploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads";
            String typeSpecificDir = baseUploadDir + "/" + fileType;
            
            // 2. 타입별 폴더 생성
            File dir = new File(typeSpecificDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            System.out.println("Upload directory: " + typeSpecificDir); // 디버깅용
            
            // 3. 유니크한 파일명 생성 (확장자 유지)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = java.util.UUID.randomUUID().toString() + extension;
            
            // 4. 파일 저장
            String savedPath = typeSpecificDir + "/" + uniqueFilename;
            file.transferTo(new File(savedPath));
            
            // 5. 접근 가능한 URL 반환 (타입 폴더 포함)
            return ResponseEntity.ok("/uploads/" + fileType + "/" + uniqueFilename);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 파일의 MIME 타입을 기반으로 저장할 폴더를 결정합니다.
     */
    private String determineFileType(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return "images";
            } else if (contentType.startsWith("video/")) {
                return "videos";
            } else if (contentType.startsWith("audio/")) {
                return "audios";
            }
        }
        
        // MIME 타입으로 판단이 안 되면 확장자로 판단
        if (originalFilename != null) {
            String extension = originalFilename.toLowerCase();
            if (extension.endsWith(".jpg") || extension.endsWith(".jpeg") || 
                extension.endsWith(".png") || extension.endsWith(".gif") || 
                extension.endsWith(".webp") || extension.endsWith(".svg")) {
                return "images";
            } else if (extension.endsWith(".mp4") || extension.endsWith(".webm") || 
                       extension.endsWith(".avi") || extension.endsWith(".mov") || 
                       extension.endsWith(".wmv") || extension.endsWith(".flv")) {
                return "videos";
            } else if (extension.endsWith(".mp3") || extension.endsWith(".wav") || 
                       extension.endsWith(".ogg") || extension.endsWith(".m4a") || 
                       extension.endsWith(".flac") || extension.endsWith(".aac")) {
                return "audios";
            }
        }
        
        // 기본값: documents 폴더
        return "documents";
    }
    
    /**
     * 파일 호환성을 위한 fallback 처리
     * 기존 루트 uploads 파일이 없으면 타입별 폴더에서 찾아서 제공
     */
    @GetMapping("/uploads/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        String baseUploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads";
        
        // 1. 먼저 루트 uploads 폴더에서 찾기 (기존 파일들)
        File rootFile = new File(baseUploadDir + "/" + filename);
        if (rootFile.exists()) {
            return ResponseEntity.ok()
                    .contentType(getMediaTypeForFile(filename))
                    .body(new FileSystemResource(rootFile));
        }
        
        // 2. 루트에 없으면 타입별 폴더들에서 찾기
        String[] typefolders = {"images", "videos", "audios", "documents"};
        for (String folder : typefolders) {
            File typeFile = new File(baseUploadDir + "/" + folder + "/" + filename);
            if (typeFile.exists()) {
                return ResponseEntity.ok()
                        .contentType(getMediaTypeForFile(filename))
                        .body(new FileSystemResource(typeFile));
            }
        }
        
        // 3. 어디에도 없으면 404
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 파일 확장자를 기반으로 MediaType을 결정합니다.
     */
    private MediaType getMediaTypeForFile(String filename) {
        String extension = filename.toLowerCase();
        if (extension.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (extension.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (extension.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (extension.endsWith(".mp4")) return MediaType.parseMediaType("video/mp4");
        if (extension.endsWith(".webm")) return MediaType.parseMediaType("video/webm");
        if (extension.endsWith(".mp3")) return MediaType.parseMediaType("audio/mp3");
        if (extension.endsWith(".wav")) return MediaType.parseMediaType("audio/wav");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
} 