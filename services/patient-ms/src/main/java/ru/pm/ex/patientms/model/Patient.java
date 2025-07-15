package ru.pm.ex.patientms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID patientId;

    @NotNull
    private String name;

    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    private String address;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
