package com.project.back_end.service;

import com.project.back_end.model.Doctor;
import com.project.back_end.model.Appointment;
import com.project.back_end.model.Login;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.AppointmentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // Get available slots for a doctor on a particular date
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Appointment> appointments =
                appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                        doctorId, start, end);

        List<String> bookedSlots = appointments.stream()
                .map(Appointment::getAppointmentTime)
                .map(LocalDateTime::toLocalTime)
                .map(LocalTime::toString)
                .collect(Collectors.toList());

        Optional<Doctor> doctor = doctorRepository.findById(doctorId);

        if (doctor.isEmpty() || doctor.get().getAvailableSlots() == null) {
            return new ArrayList<>();
        }

        return doctor.get().getAvailableSlots().stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    // Save a new doctor
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Update an existing doctor
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null
                    || !doctorRepository.existsById(doctor.getId())) {
                return -1;
            }

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Retrieve all doctors
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    // Delete a doctor and associated appointments
    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }

            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Validate doctor login
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();

        try {
            Doctor doctor = doctorRepository.findByEmail(login.getEmail());

            if (doctor == null
                    || !doctor.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            // Adjust this call to match your TokenService API.
            String token = tokenService.generateToken(doctor.getEmail());

            response.put("message", "Login successful");
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Unable to validate doctor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    // Find doctors by partial name
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findByNameLike(
                "%" + name + "%");

        response.put("doctors", doctors);
        return response;
    }

    // Filter by name, specialty, and AM/PM
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(
            String name, String specialty, String amOrPm) {

        List<Doctor> doctors =
                doctorRepository
                        .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                                name, specialty);

        Map<String, Object> response = new HashMap<>();
        response.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return response;
    }

    // Filter by name and AM/PM
    public Map<String, Object> filterDoctorByNameAndTime(
            String name, String amOrPm) {

        List<Doctor> doctors =
                doctorRepository.findByNameContainingIgnoreCase(name);

        Map<String, Object> response = new HashMap<>();
        response.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return response;
    }

    // Filter by name and specialty
    public Map<String, Object> filterDoctorByNameAndSpecility(
            String name, String specilty) {

        Map<String, Object> response = new HashMap<>();

        response.put("doctors",
                doctorRepository
                        .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                                name, specilty));

        return response;
    }

    // Filter by specialty and AM/PM
    public Map<String, Object> filterDoctorByTimeAndSpecility(
            String specilty, String amOrPm) {

        List<Doctor> doctors =
                doctorRepository.findBySpecialtyIgnoreCase(specilty);

        Map<String, Object> response = new HashMap<>();
        response.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return response;
    }

    // Filter by specialty
    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        Map<String, Object> response = new HashMap<>();

        response.put("doctors",
                doctorRepository.findBySpecialtyIgnoreCase(specilty));

        return response;
    }

    // Filter all doctors by AM/PM
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        response.put("doctors",
                filterDoctorByTime(doctorRepository.findAll(), amOrPm));

        return response;
    }

    // Private helper to filter doctors by available time
    private List<Doctor> filterDoctorByTime(
            List<Doctor> doctors, String amOrPm) {

        if (amOrPm == null
                || (!amOrPm.equalsIgnoreCase("AM")
                && !amOrPm.equalsIgnoreCase("PM"))) {
            return new ArrayList<>();
        }

        return doctors.stream()
                .filter(doctor -> doctor.getAvailableSlots() != null)
                .filter(doctor -> doctor.getAvailableSlots().stream()
                        .anyMatch(slot -> matchesTimePeriod(slot, amOrPm)))
                .collect(Collectors.toList());
    }

    private boolean matchesTimePeriod(String slot, String amOrPm) {
        try {
            LocalTime time = LocalTime.parse(slot);

            if (amOrPm.equalsIgnoreCase("AM")) {
                return time.isBefore(LocalTime.NOON);
            }

            return !time.isBefore(LocalTime.NOON);

        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
