# User Stories — Healthcare Clinic Management System

## User Story Template

**Title:**  
_As a [user role], I want [feature/goal], so that [reason]._

**Acceptance Criteria:**
1. [Criteria 1]
2. [Criteria 2]
3. [Criteria 3]

**Priority:** [High/Medium/Low]  
**Story Points:** [Estimated Effort in Points]  
**Notes:**
- [Additional information or edge cases]

---

## Doctor User Stories

### Story 1: View Appointments

**Title:**  
_As a Doctor, I want to view my scheduled appointments, so that I can prepare for patient consultations._

**Acceptance Criteria:**
1. The doctor can view their scheduled appointments.
2. Each appointment displays the date, time, patient, and status.
3. The doctor cannot access appointments assigned to another doctor without authorization.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- Display appointments in chronological order.

### Story 2: Manage Availability

**Title:**  
_As a Doctor, I want to manage my available time slots, so that patients can book appointments when I am available._

**Acceptance Criteria:**
1. The doctor can add and update available time slots.
2. The system prevents conflicting availability entries.
3. Updated availability is reflected in patient searches.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Existing confirmed appointments must be protected from accidental removal.

### Story 3: Create Prescriptions

**Title:**  
_As a Doctor, I want to create prescriptions for patients, so that they receive appropriate treatment instructions._

**Acceptance Criteria:**
1. The doctor can create a prescription for a valid appointment.
2. The prescription includes medication, dosage, and instructions.
3. Only authorized doctors can create or update prescriptions.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Validate prescription details before saving.

---

## Patient User Stories

### Story 1: Register and Log In

**Title:**  
_As a Patient, I want to register and log in securely, so that I can access my clinic account._

**Acceptance Criteria:**
1. A new patient can register using the required information.
2. The system validates input and prevents duplicate accounts.
3. A registered patient can log in using valid credentials.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Passwords must be securely hashed before storage.

### Story 2: Search for Doctors

**Title:**  
_As a Patient, I want to search for doctors by name or specialty, so that I can find a suitable healthcare provider._

**Acceptance Criteria:**
1. The patient can search by doctor name or specialty.
2. The system displays matching doctors and available appointment times.
3. The system displays a helpful message when no results are found.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- Do not expose private patient information.

### Story 3: Book and Manage Appointments

**Title:**  
_As a Patient, I want to book, view, and cancel appointments, so that I can manage my clinic visits._

**Acceptance Criteria:**
1. The patient can book an available appointment slot.
2. The system prevents multiple patients from booking the same slot.
3. The patient can view and cancel their own appointments according to clinic rules.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Patients must not access another patient's appointments.

### Story 4: View Prescriptions

**Title:**  
_As a Patient, I want to view prescriptions from my appointments, so that I can follow my treatment instructions._

**Acceptance Criteria:**
1. The patient can view prescriptions associated with their appointments.
2. Prescription details include medication and usage instructions.
3. The patient cannot access another patient's prescriptions.

**Priority:** Medium  
**Story Points:** 3  
**Notes:**
- Display a message when no prescription is available.

---

## Admin User Stories

### Story 1: Manage Doctor Accounts

**Title:**  
_As an Admin, I want to create, view, and update doctor accounts, so that clinic staff information remains accurate._

**Acceptance Criteria:**
1. The admin can create and update doctor profiles.
2. The system validates required fields and prevents duplicate accounts.
3. Only authorized admins can perform administrative changes.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Preserve historical appointment records when a doctor account is deactivated.

### Story 2: Manage Patient Records

**Title:**  
_As an Admin, I want to manage patient records, so that patient information remains accurate and up to date._

**Acceptance Criteria:**
1. The admin can find and view authorized patient records.
2. The system validates patient information when records are updated.
3. Only authorized users can access or modify patient records.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Protect confidential patient information.

### Story 3: View Appointment Reports

**Title:**  
_As an Admin, I want to view daily appointment reports by doctor, so that I can monitor clinic activity._

**Acceptance Criteria:**
1. The admin can request an appointment report for a specified date.
2. The report displays appointment details grouped or identified by doctor.
3. The system handles dates with no appointments and restricts report access to authorized admins.

**Priority:** Medium  
**Story Points:** 5  
**Notes:**
- Ensure report data matches the database records.

### Story 4: Manage Appointments

**Title:**  
_As an Admin, I want to view and manage clinic appointments, so that I can help resolve scheduling issues._

**Acceptance Criteria:**
1. The admin can search appointments by date, doctor, or status.
2. The system validates permitted appointment changes and cancellations.
3. Appointment updates are saved consistently.

**Priority:** Medium  
**Story Points:** 5  
**Notes:**
- Prevent conflicting bookings and preserve appointment history.
