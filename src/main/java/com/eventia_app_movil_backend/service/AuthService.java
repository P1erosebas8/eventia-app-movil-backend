package com.eventia_app_movil_backend.service;

import com.eventia_app_movil_backend.config.JwtService;
import com.eventia_app_movil_backend.dto.AuthRequest;
import com.eventia_app_movil_backend.dto.AuthResponse;
import com.eventia_app_movil_backend.dto.RegisterRequest;
import com.eventia_app_movil_backend.dto.UserDTO;
import com.eventia_app_movil_backend.model.User;
import com.eventia_app_movil_backend.model.UserRole;
import com.eventia_app_movil_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con este correo: " + request.getEmail());
        }

        String dni = request.resolveDni();
        if (userRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con este número de documento: " + dni);
        }

        UserRole role = UserRole.CLIENT;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = UserRole.ADMIN;
        }

        User user = User.builder()
                .firstName(request.resolveFirstName())
                .lastName(request.resolveLastName())
                .email(request.getEmail().trim().toLowerCase())
                .dni(dni)
                .password(passwordEncoder.encode(request.getPassword()))
                .cellphone(request.resolvePhone())
                .role(role)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .success(true)
                .message("Usuario registrado exitosamente")
                .token(token)
                .user(mapToUserDTO(savedUser))
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Correo o contraseña incorrectos.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!user.isActive()) {
            throw new IllegalStateException("Esta cuenta se encuentra inactiva. Contacta al administrador.");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .success(true)
                .message("Inicio de sesión exitoso")
                .token(token)
                .user(mapToUserDTO(user))
                .build();
    }

    public UserDTO mapToUserDTO(User user) {
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

