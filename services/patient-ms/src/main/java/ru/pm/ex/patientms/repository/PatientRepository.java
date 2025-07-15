package ru.pm.ex.patientms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pm.ex.patientms.model.Patient;

import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
}
