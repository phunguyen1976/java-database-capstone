package com.project.back_end.services;

import com.project.back_end.model.Appointment;
import com.project.back_end.model.Patient;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.PatientRepository;
import com.project.back_end.repository.DoctorRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService) {

        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    // Book a new appointment
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Update an existing appointment
    public ResponseEntity<Map<String, String>> updateAppointment(
            Appointment appointment) {

        Map<String, String> response = new HashMap<>();

        Optional<Appointment> existingAppointment =
                appointmentRepository.findById(appointment.getId());

        if (existingAppointment.isEmpty()) {
            response.put("message", "Appointment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        try {
            // Add your project's appointment validation here.
            appointmentRepository.save(appointment);

            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Failed to update appointment");
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Cancel an existing appointment
    public ResponseEntity<Map<String, String>> cancelAppointment(
            long id, String token) {

        Map<String, String> response = new HashMap<>();

        Optional<Appointment> appointment =
                appointmentRepository.findById(id);

        if (appointment.isEmpty()) {
            response.put("message", "Appointment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        /*
         * Verify the token and confirm that the requesting patient
         * owns this appointment before deleting it.
         *
         * Use your existing TokenService methods and your project's
         * patient/appointment ID fields for this authorization check.
         */

        try {
            appointmentRepository.delete(appointment.get());

            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Failed to cancel appointment");
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Retrieve appointments for a doctor on a specific date
    public Map<String, Object> getAppointment(
            String pname, LocalDate date, String token) {

        Map<String, Object> response = new HashMap<>();

        /*
         * Extract the doctor ID from the authorization token
         * using your existing TokenService implementation.
         */
        Long doctorId = null; // Replace with the actual token extraction.

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Appointment> appointments =
                appointmentRepository
                        .findByDoctorIdAndAppointmentTimeBetween(
                                doctorId, start, end);

        if (pname != null && !pname.trim().isEmpty()) {
            appointments = appointments.stream()
                    .filter(a -> a.getPatient() != null
                            && a.getPatient().getName() != null
                            && a.getPatient().getName()
                                    .equalsIgnoreCase(pname.trim()))
                    .toList();
        }

        response.put("appointments", appointments);
        return response;
    }
}
