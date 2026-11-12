package com.eventia_app_movil_backend.repository;

import com.eventia_app_movil_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByDni(String dni);
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
}

