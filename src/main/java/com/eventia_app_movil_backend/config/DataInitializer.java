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

        // 3. 6 Eventos Iniciales con sus Tipos de Entrada
        if (eventRepository.count() == 0) {
            log.info("[DataInitializer] Creando los 6 eventos iniciales con sus tipos de entrada (General, VIP, VIP+)...");
            User organizer = userRepository.findByEmail("admin@eventia.com")
                    .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

            Category catFestivales = categoryRepository.findByNameIgnoreCase("Festivales")
                    .orElseGet(() -> categoryRepository.save(Category.builder().name("Festivales").description("Festivales").active(true).build()));
            Category catConciertos = categoryRepository.findByNameIgnoreCase("Conciertos")
                    .orElseGet(() -> categoryRepository.save(Category.builder().name("Conciertos").description("Conciertos").active(true).build()));
            Category catTeatro = categoryRepository.findByNameIgnoreCase("Teatro & Artes")
                    .orElseGet(() -> categoryRepository.save(Category.builder().name("Teatro & Artes").description("Teatro").active(true).build()));
            Category catTech = categoryRepository.findByNameIgnoreCase("Tecnología & Negocios")
                    .orElseGet(() -> categoryRepository.save(Category.builder().name("Tecnología & Negocios").description("Tech").active(true).build()));
            Category catGastro = categoryRepository.findByNameIgnoreCase("Gastronomía")
                    .orElseGet(() -> categoryRepository.save(Category.builder().name("Gastronomía").description("Gastronomía").active(true).build()));

            // 1. Festival Neon Wave 2026
            Event ev1 = eventRepository.save(Event.builder()
                    .title("Festival Neon Wave 2026")
                    .description("El festival más grande de música electrónica, sintetizadores e iluminación visual de Lima con DJs internacionales.")
                    .bannerUrl("https://images.unsplash.com/photo-1514525253161-7a46d19cd819")
                    .capacity(600)
                    .date(LocalDate.of(2026, 11, 20))
                    .startTime(LocalTime.of(16, 30))
                    .endTime(LocalTime.of(23, 30))
                    .location("Arena 1 Costa Verde, San Miguel")
                    .status(EventStatus.PUBLISHED)
                    .active(true)
                    .category(catFestivales)
                    .organizer(organizer)
                    .build());
            createStandardTickets(ev1, 85.0, 350, 175.0, 200, 280.0, 50);

            // 2. Peru Tech & AI Summit 2026
            Event ev2 = eventRepository.save(Event.builder()
                    .title("Peru Tech & AI Summit 2026")
                    .description("Congreso líder en inteligencia artificial, desarrollo cloud y transformación digital con ponencias magistrales y feria de innovación.")
                    .bannerUrl("https://images.unsplash.com/photo-1540575467063-178a50c2df87")
                    .capacity(400)
                    .date(LocalDate.of(2026, 12, 5))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .location("Centro de Convenciones de Lima, San Borja")
                    .status(EventStatus.PUBLISHED)
                    .active(true)
                    .category(catTech)
                    .organizer(organizer)
                    .build());
            createStandardTickets(ev2, 120.0, 250, 250.0, 120, 450.0, 30);

            // 3. Noche de Jazz & Maridaje Culinario
            Event ev3 = eventRepository.save(Event.builder()
                    .title("Noche de Jazz & Maridaje Culinario")
                    .description("Una velada sofisticada de jazz contemporáneo en vivo acompañado de una degustación gastronómica y cata de vinos peruanos.")
                    .bannerUrl("https://images.unsplash.com/photo-1511192336575-5a79af67a629")
                    .capacity(150)
                    .date(LocalDate.of(2026, 11, 28))
                    .startTime(LocalTime.of(19, 30))
                    .endTime(LocalTime.of(23, 0))
                    .location("Huaca Pucllana, Miraflores")
                    .status(EventStatus.PUBLISHED)
                    .active(true)
                    .category(catGastro)
                    .organizer(organizer)
                    .build());
            createStandardTickets(ev3, 95.0, 80, 160.0, 50, 220.0, 20);

            // 4. Hamlet: Experiencia Teatral Inmersiva
            Event ev4 = eventRepository.save(Event.builder()
                    .title("Hamlet: Experiencia Teatral Inmersiva")
                    .description("La clásica obra de Shakespeare reinterpretada con una puesta en escena inmersiva 360°, orquesta en vivo y primeros actores.")
                    .bannerUrl("https://images.unsplash.com/photo-1507676184212-d03ab07a01bf")
                    .capacity(500)
                    .date(LocalDate.of(2026, 12, 12))
                    .startTime(LocalTime.of(20, 0))
                    .endTime(LocalTime.of(22, 30))
                    .location("Gran Teatro Nacional, San Borja")
                    .status(EventStatus.PUBLISHED)
                    .active(true)
                    .category(catTeatro)
                    .organizer(organizer)
                    .build());
            createStandardTickets(ev4, 65.0, 300, 130.0, 150, 200.0, 50);

            // 5. Indie Rock & Pop Sessions
            Event ev5 = eventRepository.save(Event.builder()
                    .title("Indie Rock & Pop Sessions")
                    .description("El encuentro musical indie del año con las bandas más representativas de la escena alternativa local e internacional.")
                    .bannerUrl("https://images.unsplash.com/photo-1470225620780-dba8ba36b745")
                    .capacity(700)
                    .date(LocalDate.of(2026, 12, 18))
                    .startTime(LocalTime.of(18, 0))
                    .endTime(LocalTime.of(1, 0))
                    .location("Parque de la Exposición, Lima")
                    .status(EventStatus.PUBLISHED)
                    .active(true)
                    .category(catConciertos)
                    .organizer(organizer)
                    .build());
            createStandardTickets(ev5, 70.0, 450, 140.0, 200, 220.0, 50);

            // 6. Lima Startup Weekend & Founders Pitch
            Event ev6 = eventRepository.save(Event.builder()
                    .title("Lima Startup Weekend & Founders Pitch")
                    .description("3 días intensivos para crear una startup desde cero, mentores de Silicon Valley y ronda de inversión ante fondos de Venture Capital.")
                    .bannerUrl("https://images.unsplash.com/photo-1475721027785-f74eccf877e2")
                    .capacity(250)
                    .date(LocalDate.of(2027, 1, 15))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(19, 0))
                    .location("Campus UTEC, Barranco")
                    .status(EventStatus.PUBLISHED)
                    .active(true)
                    .category(catTech)
                    .organizer(organizer)
                    .build());
            createStandardTickets(ev6, 50.0, 150, 110.0, 80, 180.0, 20);
        }

        log.info("[DataInitializer] Inicialización completada exitosamente.");
    }

    private void createStandardTickets(Event event, double pGeneral, int capGeneral,
                                       double pVip, int capVip,
                                       double pVipPlus, int capVipPlus) {
        ticketTypeRepository.save(TicketType.builder()
                .name("General")
                .price(pGeneral)
                .totalCapacity(capGeneral)
                .availableCapacity(capGeneral)
                .zone("General")
                .isPresale(false)
                .maxPerPurchase(10)
                .active(true)
                .event(event)
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .name("VIP")
                .price(pVip)
                .totalCapacity(capVip)
                .availableCapacity(capVip)
                .zone("VIP")
                .isPresale(false)
                .maxPerPurchase(10)
                .active(true)
                .event(event)
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .name("VIP+")
                .price(pVipPlus)
                .totalCapacity(capVipPlus)
                .availableCapacity(capVipPlus)
                .zone("VIP+")
                .isPresale(false)
                .maxPerPurchase(10)
                .active(true)
                .event(event)
                .build());
    }
}

