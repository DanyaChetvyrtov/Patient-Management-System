package ru.pm.ex.patientms.exception.exceptions;

public class PatientNotFound extends RuntimeException {
    public PatientNotFound() {
        super("Patient not found");
    }

    public PatientNotFound(String message) {
        super(message);
    }
}
