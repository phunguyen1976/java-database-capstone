package com.project.back_end.service;

import com.project.back_end.model.Admin;
import com.project.back_end.model.Doctor;
import com.project.back_end.model.Patient;
import com.project.back_end.repository.AdminRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String secret;

    public TokenService(
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Generates a JWT token for an admin, doctor, or patient.
     * The identifier is a username for Admin and an email for
     * Doctor or Patient.
     */
    public String generateToken(String identifier) {
        Date now = new Date();
        Date expiration = new Date(
                now.getTime() + 7L * 24 * 60 * 60 * 1000
        );

        return Jwts.builder()
                .setSubject(identifier)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username or email from a JWT token.
     */
    public String extractIdentifier(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Validates the JWT token and verifies that the user exists.
     *
     * @param token JWT token
     * @param user  admin, doctor, or patient
     * @return true if valid; otherwise false
     */
    public boolean validateToken(String token, String user) {
        if (token == null || token.trim().isEmpty()
                || user == null || user.trim().isEmpty()) {
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);

            // Explicitly check expiration.
            if (claims.getExpiration() == null
                    || claims.getExpiration().before(new Date())) {
                return false;
            }

            String identifier = claims.getSubject();

            if (identifier == null || identifier.trim().isEmpty()) {
                return false;
            }

            switch (user.toLowerCase(Locale.ROOT)) {
                case "admin":
                    Admin admin =
                            adminRepository.findByUsername(identifier);
                    return admin != null;

                case "doctor":
                    Doctor doctor =
                            doctorRepository.findByEmail(identifier);
                    return doctor != null;

                case "patient":
                    Patient patient =
                            patientRepository.findByEmail(identifier);
                    return patient != null;

                default:
                    return false;
            }

        } catch (JwtException | IllegalArgumentException e) {
            // Invalid signature, malformed token, or expired token.
            return false;
        }
    }

    /**
     * Parses the token and returns its claims.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Creates the signing key from application.properties.
     */
    public SecretKey getSigningKey() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                    "jwt.secret must be configured in application.properties"
            );
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // HS256 requires a key of at least 32 bytes.
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 bytes long"
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
