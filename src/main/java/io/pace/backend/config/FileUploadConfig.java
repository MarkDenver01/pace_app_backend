package io.pace.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class FileUploadConfig {

    private final String uploadFolder = "uploads/folders/";

    @PostConstruct
    public void init() {
        File folder = new File(uploadFolder);
        if (!folder.exists()) {
            folder.mkdirs();
            System.out.println("Created upload folder: " + folder.getAbsolutePath());
        }
    }

    public String getUploadFolder() {
        return uploadFolder;
    }
}
