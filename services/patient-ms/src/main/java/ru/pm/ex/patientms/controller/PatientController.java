package ru.pm.ex.patientms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.pm.ex.patientms.dto.PatientDto;
import ru.pm.ex.patientms.mapper.PatientMapper;
import ru.pm.ex.patientms.service.PatientService;
import ru.pm.ex.patientms.validation.OnCreate;
import ru.pm.ex.patientms.validation.OnUpdate;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientDto>> getPatients() {
        var patientDTOs = patientService.getPatients()
                .stream().map(patientMapper::toDto).toList();
        return ResponseEntity.ok().body(patientDTOs);
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Get Patient by id")
    public ResponseEntity<PatientDto> getPatient(@PathVariable UUID patientId) {
        var patient = patientService.getPatient(patientId);
        return ResponseEntity.ok().body(patientMapper.toDto(patient));
    }

    @PostMapping
    @Operation(summary = "Create Patient")
    public ResponseEntity<PatientDto> createPatient(@RequestBody @Validated(OnCreate.class) PatientDto patientDto) {
        var patient = patientMapper.toEntity(patientDto);
        patient = patientService.create(patient);
        return ResponseEntity
                .created(URI.create("/patients/" + patient.getPatientId()))
                .body(patientMapper.toDto(patient));
    }

    @PutMapping("/{patientId}")
    @Operation(summary = "Update Patient")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable("patientId") UUID patientId, @RequestBody @Validated(OnUpdate.class) PatientDto patientDto) {
        var patient = patientMapper.toEntity(patientDto);
        patient = patientService.update(patientId, patient);
        return ResponseEntity.ok().body(patientMapper.toDto(patient));
    }

    @DeleteMapping("/{patientId}")
    @Operation(summary = "Delete Patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID patientId) {
        patientService.delete(patientId);
        return ResponseEntity.noContent().build();
    }
}
