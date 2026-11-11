package com.eventia_app_movil_backend.repository;

import com.eventia_app_movil_backend.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEventIdAndActiveTrue(Long eventId);
}

