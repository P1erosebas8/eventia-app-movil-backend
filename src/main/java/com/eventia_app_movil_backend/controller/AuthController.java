package com.eventia_app_movil_backend.controller;

import com.eventia_app_movil_backend.dto.AuthRequest;
import com.eventia_app_movil_backend.dto.AuthResponse;
import com.eventia_app_movil_backend.dto.RegisterRequest;
import com.eventia_app_movil_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({"/auth", "/api/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.eventia_app_movil_backend.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.eventia_app_movil_backend.service.GmailApiService gmailApiService;

    private static class OtpRecord {
        final String code;
        final Instant expiresAt;
        int attempts;

        public OtpRecord(String code, Instant expiresAt, int attempts) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.attempts = attempts;
        }
    }

    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Paso 1: Solicitar código OTP de recuperación de contraseña.
     * Valida que el correo exista en la base de datos y despacha el OTP vía Gmail API (HTTPS 443).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El correo electrónico es requerido."
            ));
        }

        String normEmail = email.trim().toLowerCase();
        var userOpt = userRepository.findByEmailIgnoreCase(normEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "No encontramos una cuenta registrada con este correo electrónico."
            ));
        }

        var user = userOpt.get();

        // Generar código numérico de 6 dígitos
        String otpCode = String.format("%06d", secureRandom.nextInt(1_000_000));
        // Validez: 15 minutos
        otpStore.put(normEmail, new OtpRecord(otpCode, Instant.now().plus(15, ChronoUnit.MINUTES), 0));

        // Enviar OTP mediante Gmail API REST (HTTPS puerto 443 compatible con Render)
        gmailApiService.sendOtpEmail(user.getEmail(), user.getFirstName(), otpCode);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hemos enviado un código OTP de 6 dígitos a tu correo electrónico vía Gmail.",
                "email", user.getEmail(),
                "otp", otpCode
        ));
    }

    /**
     * Validador intermedio de código OTP (opcional)
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Correo y código OTP son requeridos."
            ));
        }

        String normEmail = email.trim().toLowerCase();
        OtpRecord record = otpStore.get(normEmail);

        if (record == null || Instant.now().isAfter(record.expiresAt)) {
            otpStore.remove(normEmail);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El código OTP ha expirado o no es válido. Solicita uno nuevo."
            ));
        }

        if (!record.code.equals(code.trim())) {
            record.attempts++;
            if (record.attempts >= 5) {
                otpStore.remove(normEmail);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Has superado el número máximo de intentos. Solicita un nuevo código."
                ));
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El código OTP ingresado es incorrecto."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Código OTP verificado correctamente."
        ));
    }

    /**
     * Paso 2: Restablecer contraseña validando el código OTP.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String newPassword = body.get("newPassword");

        if (email == null || code == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El correo, el código OTP y la nueva contraseña son requeridos."
            ));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "La nueva contraseña debe tener al menos 6 caracteres."
            ));
        }

        String normEmail = email.trim().toLowerCase();
        var userOpt = userRepository.findByEmailIgnoreCase(normEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "No encontramos una cuenta registrada con este correo electrónico."
            ));
        }

        OtpRecord record = otpStore.get(normEmail);
        if (record == null || Instant.now().isAfter(record.expiresAt)) {
            otpStore.remove(normEmail);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El código OTP ha expirado o no es válido. Solicita un nuevo código."
            ));
        }

        if (!record.code.equals(code.trim())) {
            record.attempts++;
            if (record.attempts >= 5) {
                otpStore.remove(normEmail);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Has superado el límite de intentos. Solicita un nuevo código OTP."
                ));
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El código OTP ingresado no coincide."
            ));
        }

        // OTP válido -> procedemos a actualizar contraseña
        otpStore.remove(normEmail);
        var user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Notificación formal por correo vía Gmail API
        gmailApiService.sendPasswordResetNotification(user.getEmail(), user.getFirstName());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contraseña restablecida exitosamente. Ya puedes iniciar sesión con tu nueva contraseña."
        ));
    }
}

