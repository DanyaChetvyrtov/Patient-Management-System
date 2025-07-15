package ru.pm.ex.patientms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        return patientRepository.findById(patientId).orElseThrow(PatientNotFound::new);
    }

    public Patient create(Patient patient) {
        return patientRepository.save(patient);
    }
}
