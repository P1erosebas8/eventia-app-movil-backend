package com.eventia_app_movil_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Servicio de alojamiento y optimización de imágenes mediante Cloudinary.
 * Permite procesar subidas de portadas de eventos y fotos de perfil de forma segura.
 */
@Slf4j
@Service
public class CloudinaryService {

    @Value("${cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:}}")
    private String cloudName;

    @Value("${cloudinary.api-key:${CLOUDINARY_API_KEY:}}")
    private String apiKey;

    @Value("${cloudinary.api-secret:${CLOUDINARY_API_SECRET:}}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        if (cloudName != null && !cloudName.isBlank() &&
            apiKey != null && !apiKey.isBlank() &&
            apiSecret != null && !apiSecret.isBlank()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName.trim(),
                    "api_key", apiKey.trim(),
                    "api_secret", apiSecret.trim(),
                    "secure", true
            ));
            log.info("[CloudinaryService] Inicializado correctamente con cloud: {}", cloudName);
        } else {
            log.info("[CloudinaryService] Credenciales no configuradas todavía. Funcionará en modo pasarela/fallback.");
        }
    }

    /**
     * Sube un archivo MultipartFile a Cloudinary en la carpeta 'eventia'.
     * @return URL pública segura (HTTPS) de la imagen.
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen no puede estar vacío.");
        }

        if (cloudinary == null) {
            log.warn("[CloudinaryService] Intento de subida sin credenciales configuradas en el entorno.");
            throw new IllegalStateException("Cloudinary no está configurado en las variables de entorno.");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "eventia",
                "resource_type", "image"
        ));

        String secureUrl = (String) uploadResult.get("secure_url");
        log.info("[CloudinaryService] Imagen subida exitosamente: {}", secureUrl);
        return secureUrl;
    }

    /**
     * Sube un array de bytes directamente a Cloudinary.
     */
    public String uploadImageBytes(byte[] bytes, String originalFilename) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Los bytes de la imagen no pueden estar vacíos.");
        }

        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary no está configurado.");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "folder", "eventia",
                "resource_type", "image"
        ));

        return (String) uploadResult.get("secure_url");
    }

    /**
     * Sube una imagen en formato Base64 o Data URI a Cloudinary.
     */
    public String uploadBase64(String base64OrDataUri) throws IOException {
        if (base64OrDataUri == null || base64OrDataUri.isBlank()) {
            throw new IllegalArgumentException("La imagen base64 no puede estar vacía.");
        }

        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary no está configurado.");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(base64OrDataUri, ObjectUtils.asMap(
                "folder", "eventia",
                "resource_type", "image"
        ));

        return (String) uploadResult.get("secure_url");
    }

    public boolean isConfigured() {
        return cloudinary != null;
    }
}

