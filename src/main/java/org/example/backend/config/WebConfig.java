package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.example.backend.storage.FileStorageService;
import org.example.backend.storage.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sert les fichiers envoyés (photos d'annonces) en statique sous /uploads/**.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StorageProperties properties;
    private final FileStorageService storageService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Le "/" final est obligatoire : sans lui Spring traite l'emplacement comme un fichier
        String location = storageService.root().toUri().toString();
        if (!location.endsWith("/")) location += "/";
        registry.addResourceHandler(properties.publicPath() + "/**")
                .addResourceLocations(location)
                .setCachePeriod(3600 * 24 * 7);
    }
}
