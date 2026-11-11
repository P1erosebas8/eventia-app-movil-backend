package com.eventia_app_movil_backend.controller;

import com.eventia_app_movil_backend.dto.UserDTO;
import com.eventia_app_movil_backend.repository.EventRepository;
import com.eventia_app_movil_backend.repository.OrderRepository;
import com.eventia_app_movil_backend.repository.UserRepository;
import com.eventia_app_movil_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;

    @GetMapping({"/users/profile", "/api/users/profile"})
    public ResponseEntity<?> getProfile(Principal principal, @RequestParam(required = false) String email) {
        String targetEmail = principal != null ? principal.getName() : email;
        if (targetEmail == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se especificó usuario"));
        }
        try {
            return ResponseEntity.ok(userService.getUserByEmail(targetEmail));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping({"/users/profile", "/api/users/profile"})
    public ResponseEntity<?> updateProfile(@RequestBody UserDTO update, Principal principal) {
        String targetEmail = principal != null ? principal.getName() : update.getEmail();
        if (targetEmail == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Usuario no identificado"));
        }
        try {
            return ResponseEntity.ok(userService.updateUserProfile(targetEmail, update));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping({"/users/change-password", "/api/users/change-password"})
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Principal principal) {
        String email = principal != null ? principal.getName() : body.get("email");
        String oldPass = body.get("oldPassword");
        String newPass = body.get("newPassword");

        if (email == null || oldPass == null || newPass == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Parámetros incompletos"));
        }

        try {
            userService.changePassword(email, oldPass, newPass);
            return ResponseEntity.ok(Map.of("success", true, "message", "Contraseña actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Endpoints Administrativos
    @GetMapping({"/admin/users", "/api/admin/users"})
    public ResponseEntity<List<UserDTO>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping({"/admin/users/{id}/role", "/api/admin/users/{id}/role"})
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        try {
            return ResponseEntity.ok(userService.updateUserRole(id, role));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping({"/admin/users/{id}/status", "/api/admin/users/{id}/status"})
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean active = body.get("active");
        try {
            return ResponseEntity.ok(userService.updateUserStatus(id, Boolean.TRUE.equals(active)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping({"/admin/stats", "/api/admin/stats"})
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();
        long totalOrders = orderRepository.count();
        double totalRevenue = orderRepository.findAll().stream()
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                .sum();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalEvents", totalEvents,
                "totalOrders", totalOrders,
                "totalRevenue", totalRevenue
        ));
    }
}

