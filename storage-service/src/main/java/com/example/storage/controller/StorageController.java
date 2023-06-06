package com.example.storage.controller;

import com.example.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class StorageController {

    private final StorageService storageService;

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file) {
        return ResponseEntity.ok(storageService.uploadFile(file));
    }

    @GetMapping
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam String url) {
        byte[] data = storageService.downloadFile(url);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + url + "\"")
                .body(resource);
    }

    @DeleteMapping
    public ResponseEntity<Boolean> deleteFile(@RequestParam String url) {
        return ResponseEntity.ok(storageService.deleteFile(url));
    }
}
