package io.pace.backend.service.customization;

import io.pace.backend.domain.model.entity.Admin;
import io.pace.backend.domain.model.entity.Customization;
import io.pace.backend.domain.model.entity.Student;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.request.CustomizationRequest;
import io.pace.backend.domain.model.response.AdminResponse;
import io.pace.backend.domain.model.response.CustomizationResponse;
import io.pace.backend.domain.model.response.StudentResponse;
import io.pace.backend.domain.model.response.UniversityResponse;
import io.pace.backend.repository.AdminRepository;
import io.pace.backend.repository.CustomizationRepository;
import io.pace.backend.repository.StudentRepository;
import io.pace.backend.repository.UniversityRepository;
import jakarta.transaction.Transactional;
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

    @Autowired
    private UniversityRepository universityRepository;

    // Local folder where files are stored (filesystem path)
    @Value("${app.upload.folder}")
    private String UPLOAD_DIR;

    /**
     * Saves or updates a theme for a specific university.
     * This method is now consistent with the corrected data model.
     * The customization is linked directly to the University, not the Admin or Student.
     *
     * @param request The CustomizationRequest DTO containing theme details.
     * @return The response DTO with the saved theme information.
     * @throws IOException if there is an error during file saving.
     */
    @Transactional
    public CustomizationResponse saveOrUpdateTheme(CustomizationRequest request) throws IOException {
        Customization customization;

        // Fetch the University entity first. This is required to link the customization.
        University university = universityRepository.findById(Math.toIntExact(request.getUniversityId()))
                .orElseThrow(() -> new IllegalArgumentException("University with ID " + request.getUniversityId() + " not found."));

        // Check if an existing theme is being updated.
        // The Customization is owned by the University, so we find it by the University's ID.
        if (university.getCustomization() != null) {
            customization = university.getCustomization();
        } else {
            // Otherwise, create a new theme and link it to the University.
            customization = new Customization();
            customization.setUniversity(university);
        }

        // Handle the logo file upload.
        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            String logoUrl = saveLogo(request.getLogoFile());
            customization.setLogoUrl(logoUrl);
        }

        // Set the other theme properties from the request.
        customization.setThemeName(request.getThemeName());
        customization.setAboutText(request.getAboutText());

        // Save the entity.
        Customization savedCustomization = customizationRepository.save(customization);

        // Build and return the final response DTO.
        return mapToResponse(savedCustomization);
    }

    /**
     * Retrieves the theme for a specific university.
     * This method no longer requires student or admin IDs, as customization is now a property of the university.
     *
     * @param universityId The ID of the university to retrieve the theme for.
     * @return The response DTO with the theme information.
     */
    public CustomizationResponse getTheme(Long universityId) {
        // Fetch the University entity as it's the main entry point for customization.
        University university = universityRepository.findById(Math.toIntExact(universityId))
                .orElseThrow(() -> new IllegalArgumentException("University with ID " + universityId + " not found."));

        // Get the customization directly from the University entity.
        Customization customization = university.getCustomization();

        // Handle the case where the University is found, but a customization does not exist.
        if (customization == null) {
            throw new IllegalArgumentException("Theme not found for the specified university.");
        }

        // Map and return the response DTO.
        return mapToResponse(customization);
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
        // Map the University entity to its DTO
        University university = customization.getUniversity();
        UniversityResponse universityResponse = null;
        if (university != null) {
            universityResponse = new UniversityResponse();
            universityResponse.setUniversityId(university.getUniversityId());
            universityResponse.setUniversityName(university.getUniversityName());
        }

        // Build and return the final response DTO.
        return CustomizationResponse.builder()
                .customizationId(customization.getCustomizationId())
                .logoUrl(customization.getLogoUrl())
                .themeName(customization.getThemeName())
                .aboutText(customization.getAboutText())
                .universityResponse(universityResponse)
                .build();
    }
}