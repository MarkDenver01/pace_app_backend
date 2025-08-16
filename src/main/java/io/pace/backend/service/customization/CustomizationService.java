package io.pace.backend.service.customization;

import io.pace.backend.domain.model.entity.Customization;
import io.pace.backend.domain.model.request.CustomizationRequest;
import io.pace.backend.domain.model.response.CustomizationResponse;
import io.pace.backend.repository.CustomizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CustomizationService {

    @Autowired
    private CustomizationRepository customizationRepository;

    // Local folder where files are stored (filesystem path)
    @Value("${app.upload.folder}")
    private String UPLOAD_DIR;

    public CustomizationResponse saveOrUpdateTheme(CustomizationRequest request) throws IOException {
        Customization customization;

        // Check if an existing theme is being updated.
        if (request.getId() != null) {
            customization = customizationRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Theme with ID " + request.getId() + " not found."));
        } else {
            // Otherwise, create a new theme entity.
            customization = new Customization();
        }

        // Handle the logo file upload.
        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            String logoUrl = saveLogo(request.getLogoFile());
            customization.setLogoUrl(logoUrl);
        }

        // Set the other theme properties from the request.
        customization.setThemeName(request.getThemeName());
        customization.setAboutText(request.getAboutText());

        // Save the entity to the database.
        Customization savedCustomization = customizationRepository.save(customization);

        // Map the saved entity to a response DTO and return it.
        return CustomizationResponse.builder()
                .id(savedCustomization.getId())
                .logoUrl(savedCustomization.getLogoUrl())
                .themeName(savedCustomization.getThemeName())
                .aboutText(savedCustomization.getAboutText())
                .build();
    }

    public CustomizationResponse getTheme(Long id) {
        return customizationRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Theme with ID " + id + " not found."));
    }

    /**
     * Helper method to save a MultipartFile to the file system.
     *
     * @param file The MultipartFile to save.
     * @return The relative URL of the saved file.
     * @throws IOException If an error occurs during file I/O.
     */
    private String saveLogo(MultipartFile file) throws IOException {
        // Create the upload directory if it doesn't exist.
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate a unique file name to prevent collisions.
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // Save the file to the file system.
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

        // Return the URL for the saved logo.
        return "/" + UPLOAD_DIR + uniqueFileName;
    }

    /**
     * Helper method to map a Customization entity to a response DTO.
     *
     * @param customization The entity to map.
     * @return The mapped response DTO.
     */
    private CustomizationResponse mapToResponse(Customization customization) {
        return CustomizationResponse.builder()
                .id(customization.getId())
                .logoUrl(customization.getLogoUrl())
                .themeName(customization.getThemeName())
                .aboutText(customization.getAboutText())
                .build();
    }
}
