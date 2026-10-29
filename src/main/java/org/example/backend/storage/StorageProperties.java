package org.example.backend.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param uploadDir dossier disque où sont écrits les fichiers envoyés
 * @param publicPath préfixe d'URL sous lequel ils sont servis (ex : /uploads)
 */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String uploadDir, String publicPath) {}
