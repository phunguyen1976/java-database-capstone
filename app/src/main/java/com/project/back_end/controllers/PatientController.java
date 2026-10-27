package com.project.back_end.controller;

import com.project.back_end.model.Patient;
import com.project.back_end.model.Login;
import com.project.back_end.service.PatientService;
import com.project.back_end.service.Service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    public PatientController(
            PatientService patientService,
            Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    /**
     * 1. Get patient details.
     */
    @GetMapping("/{token}")
    public ResponseEntity<?> getPatientDetails(
            @PathVariable String token) {

        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired patient token"));
        }

        try {
            Object patientDetails = patientService.getPatientDetails(token);

            if (patientDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Patient not found"));
            }

            return ResponseEntity.ok(patientDetails);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error"));
        }
    }

    /**
     * 2. Register a new patient.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(
            @RequestBody Patient patient) {

        Map<String, String> response = new HashMap<>();

        try {
            boolean patientDoesNotExist = service.validatePatient(patient);

            if (!patientDoesNotExist) {
                response.put(
                        "message",
                        "Patient with email id or phone no already exist"
                );

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(response);
            }

            boolean created = patientService.createPatient(patient);

            if (created) {
                response.put("message", "Signup successful");

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(response);
            }

            response.put("message", "Internal server error");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);

        } catch (Exception e) {
            response.put("message", "Internal server error");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * 3. Patient login.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(
            @RequestBody Login login) {

        Map<String, String> response = new HashMap<>();

        try {
            String token = service.validatePatientLogin(login);

            if (token == null || token.isBlank()) {
                response.put("message", "Invalid email or password");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            response.put("message", "Login successful");
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Internal server error");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * 4. Get appointments for a patient.
     */
    @GetMapping("/{id}/{token}")
    public ResponseEntity<?> getPatientAppointments(
            @PathVariable Long id,
            @PathVariable String token) {

        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired patient token"));
        }

        try {
            Object appointments =
                    patientService.getPatientAppointment(id);

            if (appointments == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Patient appointments not found"));
            }

            return ResponseEntity.ok(appointments);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error"));
        }
    }

    /**
     * 5. Filter patient appointments.
     */
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointments(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        if (!service.validateToken(token, "patient")) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Invalid or expired patient token");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return service.filterPatient(condition, name, token);
    }
}
