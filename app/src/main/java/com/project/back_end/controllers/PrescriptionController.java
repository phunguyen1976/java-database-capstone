package com.project.back_end.controller;

import com.project.back_end.model.Prescription;
import com.project.back_end.service.PrescriptionService;
import com.project.back_end.service.Service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service;

    public PrescriptionController(
            PrescriptionService prescriptionService,
            Service service) {
        this.prescriptionService = prescriptionService;
        this.service = service;
    }

    /**
     * 1. Save a prescription.
     * Only authenticated doctors can save prescriptions.
     */
    @PostMapping("/{token}")
    public ResponseEntity<?> savePrescription(
            @PathVariable String token,
            @RequestBody Prescription prescription) {

        if (!service.validateToken(token, "doctor")) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid or expired doctor token");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        try {
            return prescriptionService.savePrescription(prescription);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to save prescription");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * 2. Retrieve a prescription by appointment ID.
     * Only authenticated doctors can retrieve prescriptions.
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        if (!service.validateToken(token, "doctor")) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid or expired doctor token");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        try {
            return prescriptionService.getPrescription(appointmentId);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve prescription");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}
