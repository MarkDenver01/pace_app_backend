package io.pace.backend.controller;

import io.pace.backend.config.FileUploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    private FileUploadConfig fileUploadConfig;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = fileUploadConfig.getUploadFolder();
            File destination = new File(uploadDir + file.getOriginalFilename());
            file.transferTo(destination);
            String fileUrl = fileUploadConfig.getUploadFolder() + file.getOriginalFilename();
            return ResponseEntity.ok("File uploaded: " + fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload file.");
        }
    }
}
