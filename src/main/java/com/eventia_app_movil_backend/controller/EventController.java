package com.eventia_app_movil_backend.controller;

import com.eventia_app_movil_backend.dto.EventDTO;
import com.eventia_app_movil_backend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({"/events", "/api/events"})
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDTO>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        List<EventDTO> events = eventService.getAllEvents(category, search);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        try {
            EventDTO event = eventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventDTO dto, Principal principal) {
        String email = principal != null ? principal.getName() : "admin@eventia.com";
        try {
            EventDTO created = eventService.createEvent(dto, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventDTO dto) {
        try {
            EventDTO updated = eventService.updateEvent(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping({"/{id}/validate-tickets", "/api/events/{id}/validate-tickets"})
    public ResponseEntity<?> validateTickets(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(eventService.validateTickets(id, payload));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok(Map.of("message", "Evento eliminado/desactivado correctamente"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

