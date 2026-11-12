package com.eventia_app_movil_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private String documentType;
    private String documentNumber;
    private String phoneNumber;
    private String role;
    private String status;
    private String avatarUrl;
    private LocalDateTime createdAt;
}


