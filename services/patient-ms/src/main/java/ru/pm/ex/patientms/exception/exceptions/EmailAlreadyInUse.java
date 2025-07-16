package ru.pm.ex.patientms.exception.exceptions;

public class EmailAlreadyInUse extends RuntimeException {
    public EmailAlreadyInUse() {
        super("Such email already in use");
    }

    public EmailAlreadyInUse(String message) {
        super(message);
    }
}
