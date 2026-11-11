package com.eventia_app_movil_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String fullName;
    private String firstName;
    private String lastName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String documentType;
    private String documentNumber;
    private String dni;
    private String phoneNumber;
    private String cellphone;
    private String role;
    private Boolean termsAccepted;

    public String resolveFirstName() {
        if (firstName != null && !firstName.isBlank()) return firstName;
        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            return parts[0];
        }
        return "Usuario";
    }

    public String resolveLastName() {
        if (lastName != null && !lastName.isBlank()) return lastName;
        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            return parts.length > 1 ? parts[1] : "";
        }
        return "";
    }

    public String resolveDni() {
        if (dni != null && !dni.isBlank()) return dni;
        if (documentNumber != null && !documentNumber.isBlank()) return documentNumber;
        return "DNI" + System.currentTimeMillis() % 100000000;
    }

    public String resolvePhone() {
        if (phoneNumber != null && !phoneNumber.isBlank()) return phoneNumber;
        return cellphone;
    }
}

