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
import java.util.Map;
import java.util.Optional;
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

        String categoryName = dto.getCategoryName() != null ? dto.getCategoryName() : dto.getCategory();

        Category category;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + dto.getCategoryId()));
        } else if (categoryName != null && !categoryName.isBlank()) {
            category = categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name(categoryName)
                            .description(categoryName)
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

        LocalTime startTime = dto.getStartTime();
        if (startTime == null && dto.getTime() != null && !dto.getTime().isBlank()) {
            try {
                startTime = LocalTime.parse(dto.getTime().trim());
            } catch (Exception ignored) {
                startTime = LocalTime.of(20, 0);
            }
        }
        if (startTime == null) {
            startTime = LocalTime.of(20, 0);
        }

        String location = dto.getLocation() != null ? dto.getLocation() : (dto.getVenue() != null ? dto.getVenue() : "Lima, Perú");

        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .bannerUrl(dto.getBannerUrl() != null ? dto.getBannerUrl() : (dto.getImageUrl() != null ? dto.getImageUrl() : "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=1200"))
                .capacity(dto.getCapacity() != null ? dto.getCapacity() : 1000)
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now().plusDays(30))
                .startTime(startTime)
                .endTime(dto.getEndTime() != null ? dto.getEndTime() : startTime.plusHours(3))
                .location(location)
                .status(EventStatus.PUBLISHED)
                .active(true)
                .category(category)
                .organizer(organizer)
                .build();

        Event savedEvent = eventRepository.save(event);

        List<TicketTypeDTO> inputTickets = (dto.getTicketTypes() != null && !dto.getTicketTypes().isEmpty())
                ? dto.getTicketTypes()
                : dto.getTickets();

        List<TicketType> ticketTypes = new ArrayList<>();
        if (inputTickets != null && !inputTickets.isEmpty()) {
            int calculatedCapacity = 0;
            for (TicketTypeDTO tDto : inputTickets) {
                int cap = tDto.getAvailable() != null ? tDto.getAvailable() :
                        (tDto.getAvailableCapacity() != null ? tDto.getAvailableCapacity() :
                                (tDto.getTotalCapacity() != null ? tDto.getTotalCapacity() : 100));
                calculatedCapacity += cap;

                TicketType tt = TicketType.builder()
                        .name(tDto.getName() != null ? tDto.getName() : "General")
                        .price(tDto.getPrice() != null ? tDto.getPrice() : 50.0)
                        .totalCapacity(cap)
                        .availableCapacity(cap)
                        .zone(tDto.getZone() != null ? tDto.getZone() : tDto.getName())
                        .isPresale(Boolean.TRUE.equals(tDto.getIsPresale()))
                        .maxPerPurchase(tDto.getMaxPerPurchase() != null ? tDto.getMaxPerPurchase() : 10)
                        .active(true)
                        .event(savedEvent)
                        .build();
                ticketTypes.add(ticketTypeRepository.save(tt));
            }
            savedEvent.setCapacity(calculatedCapacity);
            savedEvent = eventRepository.save(savedEvent);
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

        // REGLA: Si el evento ya cuenta con entradas vendidas, no se puede modificar
        boolean hasSold = false;
        if (event.getTicketTypes() != null) {
            for (TicketType tt : event.getTicketTypes()) {
                if (tt.getAvailableCapacity() != null && tt.getTotalCapacity() != null &&
                        tt.getAvailableCapacity() < tt.getTotalCapacity()) {
                    hasSold = true;
                    break;
                }
            }
        }
        if (hasSold) {
            throw new IllegalStateException("El evento ya cuenta con entradas vendidas y no puede ser modificado por seguridad y cumplimiento con los compradores.");
        }

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        else if (dto.getVenue() != null) event.setLocation(dto.getVenue());

        if (dto.getDate() != null) event.setDate(dto.getDate());
        if (dto.getStartTime() != null) event.setStartTime(dto.getStartTime());
        else if (dto.getTime() != null) {
            try {
                event.setStartTime(LocalTime.parse(dto.getTime().trim()));
            } catch (Exception ignored) {}
        }
        if (dto.getEndTime() != null) event.setEndTime(dto.getEndTime());
        if (dto.getBannerUrl() != null) event.setBannerUrl(dto.getBannerUrl());
        else if (dto.getImageUrl() != null) event.setBannerUrl(dto.getImageUrl());

        String catName = dto.getCategoryName() != null ? dto.getCategoryName() : dto.getCategory();
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + dto.getCategoryId()));
            event.setCategory(category);
        } else if (catName != null) {
            Category category = categoryRepository.findByNameIgnoreCase(catName)
                    .orElseGet(() -> categoryRepository.save(Category.builder().name(catName).description(catName).active(true).build()));
            event.setCategory(category);
        }

        // Actualizar tickets/tarifas individuales del evento en MySQL
        List<TicketTypeDTO> inputTickets = (dto.getTicketTypes() != null && !dto.getTicketTypes().isEmpty())
                ? dto.getTicketTypes()
                : dto.getTickets();

        if (inputTickets != null && !inputTickets.isEmpty()) {
            int calculatedCapacity = 0;
            List<TicketType> existingTickets = event.getTicketTypes() != null ? event.getTicketTypes() : new ArrayList<>();

            for (TicketTypeDTO tDto : inputTickets) {
                int cap = tDto.getAvailable() != null ? tDto.getAvailable() :
                        (tDto.getAvailableCapacity() != null ? tDto.getAvailableCapacity() :
                                (tDto.getTotalCapacity() != null ? tDto.getTotalCapacity() : 100));
                double price = tDto.getPrice() != null ? tDto.getPrice() : 50.0;
                String name = tDto.getName() != null ? tDto.getName().trim() : "General";

                calculatedCapacity += cap;

                // Buscar ticket existente por ID o por coincidencia de nombre
                Optional<TicketType> existingOpt = existingTickets.stream()
                        .filter(tt -> (tDto.getId() != null && tDto.getId().equals(tt.getId())) ||
                                name.equalsIgnoreCase(tt.getName()) ||
                                (name.contains("+") && tt.getName().contains("+")) ||
                                (name.equalsIgnoreCase("VIP") && tt.getName().equalsIgnoreCase("VIP")))
                        .findFirst();

                if (existingOpt.isPresent()) {
                    TicketType tt = existingOpt.get();
                    tt.setPrice(price);
                    tt.setTotalCapacity(cap);
                    tt.setAvailableCapacity(cap);
                    if (tDto.getZone() != null) tt.setZone(tDto.getZone());
                    ticketTypeRepository.save(tt);
                } else {
                    TicketType newTt = TicketType.builder()
                            .name(name)
                            .price(price)
                            .totalCapacity(cap)
                            .availableCapacity(cap)
                            .zone(tDto.getZone() != null ? tDto.getZone() : name)
                            .isPresale(Boolean.TRUE.equals(tDto.getIsPresale()))
                            .maxPerPurchase(tDto.getMaxPerPurchase() != null ? tDto.getMaxPerPurchase() : 10)
                            .active(true)
                            .event(event)
                            .build();
                    ticketTypeRepository.save(newTt);
                }
            }
            event.setCapacity(calculatedCapacity);
        } else if (dto.getCapacity() != null) {
            event.setCapacity(dto.getCapacity());
        }

        Event saved = eventRepository.save(event);
        return mapToEventDTO(saved);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + id));

        // REGLA: No eliminar si tiene ventas activas
        boolean hasSold = false;
        if (event.getTicketTypes() != null) {
            for (TicketType tt : event.getTicketTypes()) {
                if (tt.getAvailableCapacity() != null && tt.getTotalCapacity() != null &&
                        tt.getAvailableCapacity() < tt.getTotalCapacity()) {
                    hasSold = true;
                    break;
                }
            }
        }
        if (hasSold) {
            throw new IllegalStateException("El evento ya cuenta con entradas vendidas y no puede ser eliminado.");
        }

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

        int soldCount = 0;
        double totalRevenue = 0.0;
        if (event.getTicketTypes() != null) {
            for (TicketType tt : event.getTicketTypes()) {
                if (tt.isActive()) {
                    int total = tt.getTotalCapacity() != null ? tt.getTotalCapacity() : 0;
                    int avail = tt.getAvailableCapacity() != null ? tt.getAvailableCapacity() : total;
                    int sold = Math.max(0, total - avail);
                    soldCount += sold;
                    totalRevenue += sold * (tt.getPrice() != null ? tt.getPrice() : 0.0);
                }
            }
        }
        boolean hasSoldTickets = soldCount > 0;

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
                .hasSoldTickets(hasSoldTickets)
                .soldTicketsCount(soldCount)
                .totalRevenue(totalRevenue)
                .ticketTypes(ticketTypeDTOs)
                .tickets(ticketTypeDTOs)
                .build();
    }

    public TicketTypeDTO mapToTicketTypeDTO(TicketType tt) {
        int avail = tt.getAvailableCapacity() != null ? tt.getAvailableCapacity() :
                (tt.getTotalCapacity() != null ? tt.getTotalCapacity() : 0);
        int total = tt.getTotalCapacity() != null ? tt.getTotalCapacity() : avail;

        return TicketTypeDTO.builder()
                .id(tt.getId())
                .name(tt.getName())
                .price(tt.getPrice())
                .totalCapacity(total)
                .availableCapacity(avail)
                .available(avail)
                .total(total)
                .tier(tt.getName())
                .zone(tt.getZone())
                .isPresale(tt.isPresale())
                .maxPerPurchase(tt.getMaxPerPurchase())
                .saleStartDate(tt.getSaleStartDate())
                .saleEndDate(tt.getSaleEndDate())
                .active(tt.isActive())
                .build();
    }

    public Map<String, Object> validateTickets(Long eventId, Map<String, Object> payload) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return Map.of(
                    "valid", false,
                    "eventId", String.valueOf(eventId),
                    "subtotal", 0.0,
                    "totalTickets", 0,
                    "message", "Evento no encontrado."
            );
        }

        Object itemsObj = payload.get("items");
        if (!(itemsObj instanceof List<?> itemsList) || itemsList.isEmpty()) {
            return Map.of(
                    "valid", false,
                    "eventId", String.valueOf(eventId),
                    "subtotal", 0.0,
                    "totalTickets", 0,
                    "message", "No se especificaron entradas para validar."
            );
        }

        double subtotal = 0.0;
        int totalTickets = 0;
        List<TicketType> eventTickets = event.getTicketTypes() != null ? event.getTicketTypes() : List.of();

        for (Object itemObj : itemsList) {
            if (itemObj instanceof Map<?, ?> itemMap) {
                String ticketIdStr = String.valueOf(itemMap.get("ticketId"));
                int quantity = 0;
                Object qObj = itemMap.get("quantity");
                if (qObj instanceof Number num) {
                    quantity = num.intValue();
                } else if (qObj != null) {
                    try { quantity = Integer.parseInt(qObj.toString()); } catch (Exception ignored) {}
                }

                if (quantity <= 0) continue;

                TicketType found = null;
                try {
                    Long tId = Long.parseLong(ticketIdStr);
                    found = eventTickets.stream().filter(t -> t.getId().equals(tId)).findFirst().orElse(null);
                } catch (NumberFormatException ignored) {}

                if (found == null) {
                    String normId = ticketIdStr.toLowerCase();
                    found = eventTickets.stream().filter(t -> {
                        String name = (t.getName() != null ? t.getName() : "").toLowerCase();
                        if (normId.contains("vip_plus") || normId.contains("vip+") || normId.contains("plus")) return name.contains("+");
                        if (normId.contains("vip")) return name.contains("vip") && !name.contains("+");
                        if (normId.contains("general")) return name.contains("general");
                        return false;
                    }).findFirst().orElse(null);
                }

                if (found == null && !eventTickets.isEmpty()) {
                    found = eventTickets.get(0);
                }

                if (found == null) {
                    return Map.of(
                            "valid", false,
                            "eventId", String.valueOf(eventId),
                            "subtotal", 0.0,
                            "totalTickets", 0,
                            "message", "Tipo de entrada no encontrado."
                    );
                }

                int available = found.getAvailableCapacity() != null ? found.getAvailableCapacity() : 0;
                if (quantity > available) {
                    return Map.of(
                            "valid", false,
                            "eventId", String.valueOf(eventId),
                            "subtotal", 0.0,
                            "totalTickets", 0,
                            "message", "No hay suficiente disponibilidad para la entrada: " + found.getName()
                    );
                }

                double price = found.getPrice() != null ? found.getPrice() : 0.0;
                subtotal += price * quantity;
                totalTickets += quantity;
            }
        }

        return Map.of(
                "valid", true,
                "eventId", String.valueOf(eventId),
                "subtotal", subtotal,
                "totalTickets", totalTickets,
                "message", "Entradas válidas y disponibles."
        );
    }
}

