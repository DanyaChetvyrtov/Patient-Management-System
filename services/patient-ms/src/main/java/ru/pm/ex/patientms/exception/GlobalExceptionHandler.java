package ru.pm.ex.patientms.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.pm.ex.patientms.dto.response.ExceptionResponse;
import ru.pm.ex.patientms.exception.exceptions.EmailAlreadyInUse;
import ru.pm.ex.patientms.exception.exceptions.PatientNotFound;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request
    ) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        var exceptionResponse = ExceptionResponse.builder()
                .message("Validation failed")
                .status(400)
                .error("Bad request")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        exceptionResponse.setErrors(fieldErrors.stream().collect(
                Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage)
        ));

        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    @ExceptionHandler({EmailAlreadyInUse.class, PatientNotFound.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponse> handlePatientException(
            RuntimeException e, HttpServletRequest request
    ) {
        var exceptionResponse = ExceptionResponse.builder()
                .message(e.getMessage())
                .status(400)
                .error("Bad request")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("Bad request: {}", e.getMessage());

        return ResponseEntity.badRequest().body(exceptionResponse);
    }
}
