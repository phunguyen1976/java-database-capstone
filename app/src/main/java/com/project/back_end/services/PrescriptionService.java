package com.project.back_end.service;

import com.project.back_end.model.Prescription;
import com.project.back_end.repository.PrescriptionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    // Save a new prescription
    public ResponseEntity<Map<String, String>> savePrescription(
            Prescription prescription) {

        Map<String, String> response = new HashMap<>();

        try {
            prescriptionRepository.save(prescription);

            response.put("message", "Prescription saved");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);

        } catch (Exception e) {
            response.put("message", "Failed to save prescription");

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Retrieve a prescription by appointment ID
    public ResponseEntity<Map<String, Object>> getPrescription(
            Long appointmentId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Prescription prescription =
                    prescriptionRepository.findByAppointmentId(appointmentId);

            if (prescription == null) {
                response.put("message", "Prescription not found");

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            response.put("prescription", prescription);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Failed to retrieve prescription");

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
