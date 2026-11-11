package com.eventia_app_movil_backend.service;

import com.eventia_app_movil_backend.dto.EventDTO;
import com.eventia_app_movil_backend.dto.TicketTypeDTO;
import com.eventia_app_movil_backend.model.*;
import com.eventia_app_movil_backend.repository.CategoryRepository;
import com.eventia_app_movil_backend.repository.EventRepository;
import com.eventia_app_movil_backend.repository.TicketTypeRepository;
import com.eventia_app_movil_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;

    public List<EventDTO> getAllEvents(String category, String search) {
        List<Event> events;
        if ((category != null && !category.isBlank()) || (search != null && !search.isBlank())) {
            String cat = (category != null && !category.isBlank()) ? category.trim() : null;
            String q = (search != null && !search.isBlank()) ? search.trim() : null;
            events = eventRepository.searchEvents(cat, q);
        } else {
            events = eventRepository.findByActiveTrue();
        }

        return events.stream()
                .map(this::mapToEventDTO)
                .collect(Collectors.toList());
    }

    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + id));
        return mapToEventDTO(event);
    }

    @Transactional
    public EventDTO createEvent(EventDTO dto, String userEmail) {
        User organizer = userRepository.findByEmail(userEmail)
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElseThrow(
                        () -> new IllegalStateException("No hay un usuario organizador disponible")
                ));

        Category category;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + dto.getCategoryId()));
        } else if (dto.getCategoryName() != null) {
            category = categoryRepository.findByNameIgnoreCase(dto.getCategoryName())
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name(dto.getCategoryName())
                            .description(dto.getCategoryName())
                            .active(true)
                            .build()));
        } else {
            category = categoryRepository.findAll().stream().findFirst()
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name("General")
                            .description("Eventos Generales")
                            .active(true)
                            .build()));
        }

        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .bannerUrl(dto.getBannerUrl() != null ? dto.getBannerUrl() : dto.getImageUrl())
                .capacity(dto.getCapacity() != null ? dto.getCapacity() : 1000)
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now().plusDays(30))
                .startTime(dto.getStartTime() != null ? dto.getStartTime() : LocalTime.of(20, 0))
                .endTime(dto.getEndTime() != null ? dto.getEndTime() : LocalTime.of(23, 0))
                .location(dto.getLocation() != null ? dto.getLocation() : "Lima, Perú")
                .status(EventStatus.PUBLISHED)
                .active(true)
                .category(category)
                .organizer(organizer)
                .build();

        Event savedEvent = eventRepository.save(event);

        // Crear tipos de entrada por defecto si no vinieron
        List<TicketType> ticketTypes = new ArrayList<>();
        if (dto.getTicketTypes() != null && !dto.getTicketTypes().isEmpty()) {
            for (TicketTypeDTO tDto : dto.getTicketTypes()) {
                TicketType tt = TicketType.builder()
                        .name(tDto.getName())
                        .price(tDto.getPrice())
                        .totalCapacity(tDto.getTotalCapacity() != null ? tDto.getTotalCapacity() : 500)
                        .availableCapacity(tDto.getAvailableCapacity() != null ? tDto.getAvailableCapacity() : 500)
                        .zone(tDto.getZone() != null ? tDto.getZone() : tDto.getName())
                        .isPresale(Boolean.TRUE.equals(tDto.getIsPresale()))
                        .maxPerPurchase(tDto.getMaxPerPurchase() != null ? tDto.getMaxPerPurchase() : 10)
                        .active(true)
                        .event(savedEvent)
                        .build();
                ticketTypes.add(ticketTypeRepository.save(tt));
            }
        } else {
            // Standard Eventia ticket types: General, VIP, VIP+
            ticketTypes.add(ticketTypeRepository.save(TicketType.builder()
                    .name("General")
                    .price(85.0)
                    .totalCapacity(800)
                    .availableCapacity(800)
                    .zone("General")
                    .isPresale(false)
                    .active(true)
                    .event(savedEvent)
                    .build()));
            ticketTypes.add(ticketTypeRepository.save(TicketType.builder()
                    .name("VIP")
                    .price(180.0)
                    .totalCapacity(300)
                    .availableCapacity(300)
                    .zone("VIP")
                    .isPresale(false)
                    .active(true)
                    .event(savedEvent)
                    .build()));
            ticketTypes.add(ticketTypeRepository.save(TicketType.builder()
                    .name("VIP+")
                    .price(250.0)
                    .totalCapacity(100)
                    .availableCapacity(100)
                    .zone("VIP+")
                    .isPresale(false)
                    .active(true)
                    .event(savedEvent)
                    .build()));
        }

        savedEvent.setTicketTypes(ticketTypes);
        return mapToEventDTO(savedEvent);
    }

    @Transactional
    public EventDTO updateEvent(Long id, EventDTO dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + id));

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getDate() != null) event.setDate(dto.getDate());
        if (dto.getStartTime() != null) event.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) event.setEndTime(dto.getEndTime());
        if (dto.getCapacity() != null) event.setCapacity(dto.getCapacity());
        if (dto.getBannerUrl() != null) event.setBannerUrl(dto.getBannerUrl());
        else if (dto.getImageUrl() != null) event.setBannerUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + dto.getCategoryId()));
            event.setCategory(category);
        }

        return mapToEventDTO(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + id));
        event.setActive(false);
        eventRepository.save(event);
    }

    public EventDTO mapToEventDTO(Event event) {
        List<TicketTypeDTO> ticketTypeDTOs = (event.getTicketTypes() != null)
                ? event.getTicketTypes().stream()
                .filter(TicketType::isActive)
                .map(this::mapToTicketTypeDTO)
                .collect(Collectors.toList())
                : new ArrayList<>();

        String categoryName = (event.getCategory() != null) ? event.getCategory().getName() : "General";
        String organizerName = (event.getOrganizer() != null) ? event.getOrganizer().getFullName() : "Eventia Oficial";
        String timeStr = (event.getStartTime() != null) ? event.getStartTime().toString() : "20:00";

        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .date(event.getDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .time(timeStr)
                .location(event.getLocation())
                .venue(event.getLocation())
                .capacity(event.getCapacity())
                .bannerUrl(event.getBannerUrl())
                .imageUrl(event.getBannerUrl())
                .categoryId(event.getCategory() != null ? event.getCategory().getId() : null)
                .categoryName(categoryName)
                .category(categoryName)
                .organizer(organizerName)
                .status(event.getStatus() != null ? event.getStatus().name() : "PUBLISHED")
                .active(event.isActive())
                .ticketTypes(ticketTypeDTOs)
                .tickets(ticketTypeDTOs)
                .build();
    }

    public TicketTypeDTO mapToTicketTypeDTO(TicketType tt) {
        return TicketTypeDTO.builder()
                .id(tt.getId())
                .name(tt.getName())
                .price(tt.getPrice())
                .totalCapacity(tt.getTotalCapacity())
                .availableCapacity(tt.getAvailableCapacity())
                .zone(tt.getZone())
                .isPresale(tt.isPresale())
                .maxPerPurchase(tt.getMaxPerPurchase())
                .saleStartDate(tt.getSaleStartDate())
                .saleEndDate(tt.getSaleEndDate())
                .active(tt.isActive())
                .build();
    }
}

