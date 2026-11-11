package com.eventia_app_movil_backend.repository;

import com.eventia_app_movil_backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t JOIN t.orderDetail od JOIN od.order o WHERE o.user.id = :userId ORDER BY t.createdAt DESC")
    List<Ticket> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t JOIN t.orderDetail od JOIN od.order o WHERE o.user.email = :email ORDER BY t.createdAt DESC")
    List<Ticket> findByUserEmail(@Param("email") String email);
}

