package com.eventia_app_movil_backend.config;

import com.eventia_app_movil_backend.model.*;
import com.eventia_app_movil_backend.repository.CategoryRepository;
import com.eventia_app_movil_backend.repository.EventRepository;
import com.eventia_app_movil_backend.repository.TicketTypeRepository;
import com.eventia_app_movil_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("[DataInitializer] Verificando datos iniciales en eventia_db...");

        // 1. Usuarios por defecto (Soporte para .com y .io)
        userRepository.findByEmail("admin@eventia.com").ifPresentOrElse(
                u -> {
                    u.setPassword(passwordEncoder.encode("Cliente123#"));
                    u.setRole(UserRole.ADMIN);
                    u.setActive(true);
                    userRepository.save(u);
                },
                () -> userRepository.save(User.builder()
                        .firstName("Admin")
                        .lastName("Eventia")
                        .email("admin@eventia.com")
                        .dni("00000001")
                        .cellphone("999888777")
                        .password(passwordEncoder.encode("Cliente123#"))
                        .role(UserRole.ADMIN)
                        .active(true)
                        .build())
        );

        userRepository.findByEmail("admin@eventia.io").orElseGet(() ->
                userRepository.save(User.builder()
                        .firstName("Admin")
                        .lastName("Eventia")
                        .email("admin@eventia.io")
                        .dni("00000002")
                        .cellphone("999888776")
                        .password(passwordEncoder.encode("Admin123!"))
                        .role(UserRole.ADMIN)
                        .active(true)
                        .build())
        );

        userRepository.findByEmail("cliente@eventia.com").ifPresentOrElse(
                u -> {
                    u.setPassword(passwordEncoder.encode("Cliente123#"));
                    u.setRole(UserRole.CLIENT);
                    u.setActive(true);
                    userRepository.save(u);
                },
                () -> userRepository.save(User.builder()
                        .firstName("Carlos")
                        .lastName("Mendoza")
                        .email("cliente@eventia.com")
                        .dni("72345678")
                        .cellphone("987654321")
                        .password(passwordEncoder.encode("Cliente123#"))
                        .role(UserRole.CLIENT)
                        .active(true)
                        .build())
        );

        userRepository.findByEmail("cliente@eventia.io").orElseGet(() ->
                userRepository.save(User.builder()
                        .firstName("Daniela")
                        .lastName("Morales")
                        .email("cliente@eventia.io")
                        .dni("72345679")
                        .cellphone("987654322")
                        .password(passwordEncoder.encode("Cliente123!"))
                        .role(UserRole.CLIENT)
                        .active(true)
                        .build())
        );

        // 2. Categorías
        if (categoryRepository.count() == 0) {
            log.info("[DataInitializer] Creando categorías principales");
            categoryRepository.saveAll(List.of(
                    Category.builder().name("Conciertos").description("Música en vivo").active(true).build(),
                    Category.builder().name("Festivales").description("Grandes festivales al aire libre").active(true).build(),
                    Category.builder().name("Teatro & Artes").description("Obras teatrales y cultura").active(true).build(),
                    Category.builder().name("Tecnología & Negocios").description("Conferencias y networking").active(true).build(),
                    Category.builder().name("Gastronomía").description("Ferias culinarias y catas").active(true).build()
            ));
        }

        // 3. Tipos de entradas para eventos existentes
        List<Event> existingEvents = eventRepository.findAll();
        for (Event event : existingEvents) {
            List<TicketType> existingTicketTypes = ticketTypeRepository.findByEventIdAndActiveTrue(event.getId());
            if (existingTicketTypes.isEmpty()) {
                log.info("[DataInitializer] Creando tipos de entrada (General, VIP, VIP+) para evento: {}", event.getTitle());
                ticketTypeRepository.save(TicketType.builder()
                        .name("General")
                        .price(85.0)
                        .totalCapacity(500)
                        .availableCapacity(450)
                        .zone("General")
                        .isPresale(false)
                        .active(true)
                        .event(event)
                        .build());
                ticketTypeRepository.save(TicketType.builder()
                        .name("VIP")
                        .price(180.0)
                        .totalCapacity(200)
                        .availableCapacity(180)
                        .zone("VIP")
                        .isPresale(false)
                        .active(true)
                        .event(event)
                        .build());
                ticketTypeRepository.save(TicketType.builder()
                        .name("VIP+")
                        .price(250.0)
                        .totalCapacity(50)
                        .availableCapacity(40)
                        .zone("VIP+")
                        .isPresale(false)
                        .active(true)
                        .event(event)
                        .build());
            }
        }

        log.info("[DataInitializer] Inicialización completada exitosamente.");
    }
}

