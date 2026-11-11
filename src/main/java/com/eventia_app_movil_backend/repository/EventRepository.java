package com.eventia_app_movil_backend.repository;

import com.eventia_app_movil_backend.model.Event;
import com.eventia_app_movil_backend.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByActiveTrue();

    List<Event> findByStatusAndActiveTrue(EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.active = true " +
           "AND (:category IS NULL OR LOWER(e.category.name) = LOWER(:category)) " +
           "AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Event> searchEvents(@Param("category") String category, @Param("search") String search);
}

