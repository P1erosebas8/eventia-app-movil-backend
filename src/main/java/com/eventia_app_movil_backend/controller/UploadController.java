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
     * Endpoint flexible para subida de imágenes a Cloudinary:
     * - Admite multipart/form-data con archivo binario ('file')
     * - Admite application/json con base64 o data-uri ('image' o 'file')
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestBody(required = false) Map<String, String> jsonBody) {
        try {
            if (!cloudinaryService.isConfigured()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                        "success", false,
                        "message", "El servicio de Cloudinary no está configurado en las variables de entorno de Render."
                ));
            }

            String secureUrl;
            if (file != null && !file.isEmpty()) {
                secureUrl = cloudinaryService.uploadImage(file);
            } else if (jsonBody != null && (jsonBody.containsKey("image") || jsonBody.containsKey("file"))) {
                String raw = jsonBody.containsKey("image") ? jsonBody.get("image") : jsonBody.get("file");
                secureUrl = cloudinaryService.uploadBase64(raw);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Debes enviar un archivo en multipart o un base64 en JSON ('image'/'file')."
                ));
            }

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

