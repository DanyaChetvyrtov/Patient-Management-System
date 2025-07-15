package ru.pm.ex.patientms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.pm.ex.patientms.validation.OnCreate;
import ru.pm.ex.patientms.validation.OnUpdate;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientDto {
    @NotNull(message = "ID cannot be null", groups = OnUpdate.class)
    private UUID patientId;

    @NotBlank(message = "Name is required", groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 100, message = "Name cannot exceed 100 characters", groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @NotBlank(message = "Email is required", groups = {OnCreate.class, OnUpdate.class})
    @Email(message = "Email should be valid", groups = {OnCreate.class, OnUpdate.class})
    private String email;

    @NotBlank(message = "Address is required", groups = {OnCreate.class, OnUpdate.class})
    private String address;

    @NotNull(message = "Date of birth is required", groups = {OnCreate.class, OnUpdate.class})
    private LocalDate dateOfBirth;
}
