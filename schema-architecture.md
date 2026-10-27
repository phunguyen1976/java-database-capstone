# Clinic Database Schema Design

## MySQL Database Design

MySQL stores the clinic's structured operational data. Primary and foreign keys keep relationships consistent, while `NOT NULL` and `UNIQUE` constraints prevent incomplete or duplicate records.

### Table: patients

- `id`: BIGINT, Primary Key, Auto Increment
- `full_name`: VARCHAR(100), NOT NULL
- `email`: VARCHAR(254), NOT NULL, UNIQUE
- `phone`: VARCHAR(10), NOT NULL, UNIQUE
- `password_hash`: VARCHAR(255), NOT NULL
- `date_of_birth`: DATE, NULL
- `created_at`: TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP

Notes:
- Store a password hash, never a plaintext password.
- Validate email and phone format in application code as well as applying database constraints where practical.
- Prefer deactivation/anonymization over hard deletion when retention rules require preserving medical history.

### Table: doctors

- `id`: BIGINT, Primary Key, Auto Increment
- `full_name`: VARCHAR(100), NOT NULL
- `specialty`: VARCHAR(50), NOT NULL
- `email`: VARCHAR(254), NOT NULL, UNIQUE
- `phone`: VARCHAR(10), NOT NULL, UNIQUE
- `password_hash`: VARCHAR(255), NOT NULL
- `active`: BOOLEAN, NOT NULL, DEFAULT TRUE
- `created_at`: TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP

Notes:
- Validate email and phone formats in application code.
- Deactivate a doctor rather than deleting them if they have appointment history.

### Table: admin

- `id`: BIGINT, Primary Key, Auto Increment
- `username`: VARCHAR(80), NOT NULL, UNIQUE
- `email`: VARCHAR(254), NOT NULL, UNIQUE
- `password_hash`: VARCHAR(255), NOT NULL
- `created_at`: TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP

Notes:
- Use a password hash and enforce role-based access control in the application.

### Table: appointments

- `id`: BIGINT, Primary Key, Auto Increment
- `patient_id`: BIGINT, NOT NULL, Foreign Key → `patients(id)`
- `doctor_id`: BIGINT, NOT NULL, Foreign Key → `doctors(id)`
- `appointment_time`: DATETIME, NOT NULL
- `duration_minutes`: SMALLINT, NOT NULL, DEFAULT 30
- `status`: TINYINT, NOT NULL, DEFAULT 0 (`0 = Scheduled`, `1 = Completed`, `2 = Cancelled`, `3 = No-show`)
- `reason`: VARCHAR(500), NULL
- `created_at`: TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
- Index: (`doctor_id`, `appointment_time`)
- Index: (`patient_id`, `appointment_time`)

Notes:
- Use `ON DELETE RESTRICT` for patient and doctor foreign keys so historical appointments are not silently deleted. Cancel or anonymize records according to clinic retention policies instead.
- Prevent overlapping appointments for a doctor in application/service logic, using a transaction and appropriate locking. A simple unique constraint on doctor and start time prevents identical start times but does not prevent all duration overlaps.
- Validate that appointment times are in the future when booking, if required by clinic policy.

### Table: doctor_available_slots

- `id`: BIGINT, Primary Key, Auto Increment
- `doctor_id`: BIGINT, NOT NULL, Foreign Key → `doctors(id)`
- `start_time`: DATETIME, NOT NULL
- `end_time`: DATETIME, NOT NULL
- `is_bookable`: BOOLEAN, NOT NULL, DEFAULT TRUE
- Unique constraint: (`doctor_id`, `start_time`, `end_time`)

Notes:
- Each doctor can have their own availability windows.
- Ensure `end_time > start_time` in application validation or with a database CHECK constraint supported by the deployed MySQL version.
- Booking logic must ensure an appointment fits inside an available window and does not overlap another active appointment.

### Table: prescriptions

- `id`: BIGINT, Primary Key, Auto Increment
- `appointment_id`: BIGINT, NOT NULL, UNIQUE, Foreign Key → `appointments(id)`
- `doctor_id`: BIGINT, NOT NULL, Foreign Key → `doctors(id)`
- `patient_id`: BIGINT, NOT NULL, Foreign Key → `patients(id)`
- `issued_at`: DATETIME, NOT NULL, DEFAULT CURRENT_TIMESTAMP
- `status`: VARCHAR(20), NOT NULL, DEFAULT 'ACTIVE'
- `summary`: VARCHAR(1000), NULL

Notes:
- A prescription is linked to a specific appointment. The unique `appointment_id` allows at most one prescription per appointment; remove `UNIQUE` if the clinic needs multiple prescriptions per appointment.
- Use `ON DELETE RESTRICT` to preserve the medical record. Keep detailed or evolving prescription metadata in MongoDB if desired, while retaining the relational IDs in MySQL.

### Relationship and integrity decisions

- One patient can have many appointments; each appointment belongs to one patient.
- One doctor can have many appointments; each appointment belongs to one doctor.
- One doctor can have many availability slots.
- An appointment can have zero or one prescription under this design.
- Use foreign keys and transactions to keep related records consistent.
- Do not cascade-delete appointment or prescription history when a patient or doctor is deleted. Prefer a deactivation or a carefully governed anonymization/retention process.
- Email and phone syntax should be validated in application code; database `UNIQUE` constraints prevent duplicate values but do not validate their format.
- If the clinic later supports multiple locations or payments, add `clinic_locations` and `payments` tables with foreign keys to the relevant records.

## MongoDB Collection Design

### Collection: prescription_details

MongoDB complements the MySQL schema by storing flexible prescription instructions and metadata. The `appointmentId`, `patientId`, and `doctorId` values reference MySQL records; MongoDB does not enforce those foreign-key relationships, so the application should verify them.

```json
{
  "_id": { "$oid": "64abc1234567890123456789" },
  "appointmentId": 51,
  "patientId": 12,
  "doctorId": 7,
  "issuedAt": "2026-10-27T09:30:00Z",
  "medications": [
    {
      "name": "Paracetamol",
      "dosage": "500 mg",
      "route": "oral",
      "instructions": "Take as directed by the prescribing clinician",
      "refills": 0
    }
  ],
  "doctorNotes": "Follow-up if symptoms persist.",
  "pharmacy": {
    "name": "Example Community Pharmacy",
    "location": {
      "city": "Hesperia",
      "state": "CA"
    }
  },
  "tags": ["pain-relief", "follow-up"],
  "metadata": {
    "schemaVersion": 1,
    "source": "clinic-portal",
    "lastUpdatedBy": "doctor:7"
  },
  "attachments": [
    {
      "fileName": "patient-instructions.pdf",
      "storageKey": "prescriptions/51/patient-instructions.pdf",
      "contentType": "application/pdf"
    }
  ]
}
