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
     * Subida de imágenes a Cloudinary vía multipart/form-data (archivos binarios).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadMultipart(
            @RequestParam("file") MultipartFile file) {
        try {
            if (!cloudinaryService.isConfigured()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                        "success", false,
                        "message", "El servicio de Cloudinary no está configurado en las variables de entorno de Render."
                ));
            }

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Debes enviar un archivo en multipart con el parámetro 'file'."
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
            log.error("[UploadController] Error al subir imagen multipart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error al procesar la imagen: " + e.getMessage()
            ));
        }
    }

    /**
     * Subida de imágenes a Cloudinary vía application/json (cadenas base64 o data-uri).
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> uploadJson(
            @RequestBody Map<String, String> jsonBody) {
        try {
            if (!cloudinaryService.isConfigured()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                        "success", false,
                        "message", "El servicio de Cloudinary no está configurado en las variables de entorno de Render."
                ));
            }

            if (jsonBody == null || (!jsonBody.containsKey("image") && !jsonBody.containsKey("file"))) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Debes enviar un JSON con la propiedad 'image' o 'file' en formato base64."
                ));
            }

            String raw = jsonBody.containsKey("image") ? jsonBody.get("image") : jsonBody.get("file");
            String secureUrl = cloudinaryService.uploadBase64(raw);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "url", secureUrl,
                    "secure_url", secureUrl,
                    "message", "Imagen alojada exitosamente en Cloudinary"
            ));
        } catch (Exception e) {
            log.error("[UploadController] Error al subir imagen json: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error al procesar la imagen: " + e.getMessage()
            ));
        }
    }
}

