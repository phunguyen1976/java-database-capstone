package com.project.back_end.controller;

import com.project.back_end.model.Doctor;
import com.project.back_end.model.Login;
import com.project.back_end.service.DoctorService;
import com.project.back_end.service.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(
            DoctorService doctorService,
            Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    /**
     * 1. Get doctor availability.
     */
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, user)) {
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        try {
            LocalDate requestedDate = LocalDate.parse(date);

            List<String> availability =
                    doctorService.getDoctorAvailability(
                            doctorId, requestedDate
                    );

            response.put("doctorId", doctorId);
            response.put("date", requestedDate.toString());
            response.put("availability", availability);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("message", "Invalid date format. Use YYYY-MM-DD");
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("message", "Failed to retrieve doctor availability");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * 2. Get all doctors.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Doctor> doctors = doctorService.getDoctors();

            response.put("doctors", doctors);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Failed to retrieve doctors");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * 3. Add a new doctor.
     * Only admins may add doctors.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        Map<String, String> response = new HashMap<>();

        if (!service.validateToken(token, "admin")) {
            response.put("message", "Invalid or expired admin token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        try {
            boolean saved = doctorService.saveDoctor(doctor);

            if (saved) {
                response.put("message", "Doctor added to db");
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(response);
            }

            response.put("message", "Doctor already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(response);

        } catch (Exception e) {
            response.put("message", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * 4. Doctor login.
     */
    @PostMapping("/login")
    public ResponseEntity<?> doctorLogin(@RequestBody Login login) {
        try {
            return doctorService.validateDoctor(login);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Some internal error occurred");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * 5. Update doctor details.
     * Only admins may update doctor details.
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        Map<String, String> response = new HashMap<>();

        if (!service.validateToken(token, "admin")) {
            response.put("message", "Invalid or expired admin token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        try {
            boolean updated = doctorService.updateDoctor(doctor);

            if (updated) {
                response.put("message", "Doctor updated");
                return ResponseEntity.ok(response);
            }

            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(response);

        } catch (Exception e) {
            response.put("message", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * 6. Delete a doctor.
     * Only admins may delete doctors.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable Long id,
            @PathVariable String token) {

        Map<String, String> response = new HashMap<>();

        if (!service.validateToken(token, "admin")) {
            response.put("message", "Invalid or expired admin token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        try {
            boolean deleted = doctorService.deleteDoctor(id);

            if (deleted) {
                response.put("message", "Doctor deleted successfully");
                return ResponseEntity.ok(response);
            }

            response.put("message", "Doctor not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(response);

        } catch (Exception e) {
            response.put("message", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * 7. Filter doctors by name, time, and specialty.
     */
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {

        try {
            Map<String, Object> filteredDoctors =
                    service.filterDoctor(name, speciality, time);

            return ResponseEntity.ok(filteredDoctors);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to filter doctors");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}
