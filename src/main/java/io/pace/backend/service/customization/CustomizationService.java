package io.pace.backend.service.customization;

import io.pace.backend.domain.model.entity.Customization;
import io.pace.backend.domain.model.response.CustomizationResponse;
import io.pace.backend.repository.CustomizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class CustomizationService {

    @Autowired
    private CustomizationRepository customizationRepository;

    // Folder to store uploaded logos, relative to the project root
    @Value("${app.upload.folder}")
    private String uploadFolder;

    // Base URL to access uploaded files (e.g., http://localhost:8080/uploads)
    @Value("${app.upload.base-url}")
    private String uploadBaseUrl;

    public CustomizationResponse getCustomization() {
        Customization customization = customizationRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    Customization def = new Customization();
                    def.setThemeName("light");
                    def.setAboutText("");
                    return customizationRepository.save(def);
                });

        return new CustomizationResponse(
                customization.getThemeName(),
                customization.getLogoUrl(),
                customization.getAboutText()
        );
    }

    public CustomizationResponse updateCustomization(MultipartFile logo, String theme, String aboutText) throws IOException {
        Customization customization = customizationRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(Customization::new);

        if (log != null && !logo.isEmpty()) {
            File uploadDir = new File(uploadFolder);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String originalName = new File(logo.getOriginalFilename()).getName();
            String uniqueName = System.currentTimeMillis() + "_" + originalName;
            File destination = new File(uploadDir, uniqueName);

            logo.transferTo(destination);
            customization.setLogoUrl(uploadBaseUrl + "/" + uniqueName);
        }

        customization.setThemeName(theme);
        customization.setAboutText(aboutText);

        customizationRepository.save(customization);

        return new CustomizationResponse(
                customization.getThemeName(),
                customization.getLogoUrl(),
                customization.getAboutText()
        );
    }
}
