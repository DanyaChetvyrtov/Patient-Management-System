package ru.pm.ex.patientms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pm.ex.patientms.exception.exceptions.EmailAlreadyInUse;
import ru.pm.ex.patientms.exception.exceptions.PatientNotFound;
import ru.pm.ex.patientms.model.Patient;
import ru.pm.ex.patientms.repository.PatientRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    public List<Patient> getPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatient(UUID patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFound("Patient with id " + patientId + " not found"));
    }

    public Patient create(Patient patient) {
        if (patientRepository.existsByEmail(patient.getEmail()))
            throw new EmailAlreadyInUse("Email " + patient.getEmail() + " is already in use");

        return patientRepository.save(patient);
    }

    public Patient update(UUID patientId, Patient patient) {
        var dbPatient = getPatient(patientId);

        if(updatedEmailInUse(dbPatient.getEmail(), patient.getEmail()))
            throw new EmailAlreadyInUse("Email " + patient.getEmail() + " is already in use");

        dbPatient.setName(patient.getName());
        dbPatient.setEmail(patient.getEmail());
        dbPatient.setAddress(patient.getAddress());
        dbPatient.setDateOfBirth(patient.getDateOfBirth());

        return patientRepository.save(dbPatient);
    }

    private boolean updatedEmailInUse(String dbPatientEmail, String requestPatientEmail){
        return !dbPatientEmail.equals(requestPatientEmail) &&
                patientRepository.existsByEmail(requestPatientEmail);
    }

    public void delete(UUID patientId) {
        patientRepository.deleteById(patientId);
    }
}
