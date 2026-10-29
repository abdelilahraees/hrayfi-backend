package org.example.backend.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Stockage des images sur le disque local. Le nom de fichier est toujours généré
 * (UUID + extension déduite du type MIME) : le nom fourni par le client n'est
 * jamais utilisé, ce qui écarte toute traversée de répertoire.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private final StorageProperties properties;

    /** Enregistre une image dans {@code folder} et renvoie son URL publique. */
    public String storeImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }
        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Format d'image non supporté (JPEG, PNG, WEBP ou GIF uniquement)");
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path dir = root().resolve(folder).normalize();
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(dir);
            Files.copy(in, dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible d'enregistrer l'image", e);
        }
        return properties.publicPath() + "/" + folder + "/" + filename;
    }

    /** Supprime le fichier correspondant à une URL publique ; ignore les URLs étrangères. */
    public void delete(String publicUrl) {
        String prefix = properties.publicPath() + "/";
        if (publicUrl == null || !publicUrl.startsWith(prefix)) return;

        Path target = root().resolve(publicUrl.substring(prefix.length())).normalize();
        if (!target.startsWith(root())) return;
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Suppression du fichier {} impossible : {}", target, e.getMessage());
        }
    }

    public Path root() {
        return Path.of(properties.uploadDir()).toAbsolutePath().normalize();
    }
}
