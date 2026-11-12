package com.eventia_app_movil_backend.controller;

import com.eventia_app_movil_backend.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping({"/upload", "/api/upload"})
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    /**
     * Endpoint para subida de imágenes (eventos y fotos de perfil) a Cloudinary.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (!cloudinaryService.isConfigured()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                        "success", false,
                        "message", "El servicio de Cloudinary no está configurado en las variables de entorno de Render."
                ));
            }

            String secureUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "url", secureUrl,
                    "secure_url", secureUrl,
                    "message", "Imagen alojada exitosamente en Cloudinary"
            ));
        } catch (Exception e) {
            log.error("[UploadController] Error al subir imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error al procesar la imagen: " + e.getMessage()
            ));
        }
    }
}

