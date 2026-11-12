package com.eventia_app_movil_backend.controller;

import com.eventia_app_movil_backend.dto.AdminDashboardDTO;
import com.eventia_app_movil_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({"/admin", "/api/admin"})
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        AdminDashboardDTO metrics = adminService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }
}

