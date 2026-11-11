package com.eventia_app_movil_backend.service;

import com.eventia_app_movil_backend.dto.UserDTO;
import com.eventia_app_movil_backend.model.User;
import com.eventia_app_movil_backend.model.UserRole;
import com.eventia_app_movil_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        return mapToDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateUserProfile(String email, UserDTO update) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

        if (update.getFirstName() != null && !update.getFirstName().isBlank()) {
            user.setFirstName(update.getFirstName());
        }
        if (update.getLastName() != null && !update.getLastName().isBlank()) {
            user.setLastName(update.getLastName());
        }
        if (update.getFullName() != null && !update.getFullName().isBlank()) {
            String[] parts = update.getFullName().trim().split("\\s+", 2);
            user.setFirstName(parts[0]);
            if (parts.length > 1) user.setLastName(parts[1]);
        }
        if (update.getPhoneNumber() != null) {
            user.setCellphone(update.getPhoneNumber());
        }

        return mapToDTO(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserDTO updateUserRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        try {
            user.setRole(UserRole.valueOf(roleName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol no válido: " + roleName);
        }

        return mapToDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.setActive(active);
        return mapToDTO(userRepository.save(user));
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .documentType("DNI")
                .documentNumber(user.getDni())
                .phoneNumber(user.getCellphone())
                .role(user.getRole().name())
                .status(user.isActive() ? "ACTIVE" : "INACTIVE")
                .build();
    }
}

