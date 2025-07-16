package ru.pm.ex.patientms.controller;

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
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @GetMapping
    public ResponseEntity<List<PatientDto>> getPatients() {
        var patientDTOs = patientService.getPatients()
                .stream().map(patientMapper::toDto).toList();
        return ResponseEntity.ok().body(patientDTOs);
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientDto> getPatient(@PathVariable UUID patientId) {
        var patient = patientService.getPatient(patientId);
        return ResponseEntity.ok().body(patientMapper.toDto(patient));
    }

    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@RequestBody @Validated(OnCreate.class) PatientDto patientDto) {
        var patient = patientMapper.toEntity(patientDto);
        patient = patientService.create(patient);
        return ResponseEntity
                .created(URI.create("/patients/" + patient.getPatientId()))
                .body(patientMapper.toDto(patient));
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable("patientId") UUID patientId, @RequestBody @Validated(OnUpdate.class) PatientDto patientDto) {
        var patient = patientMapper.toEntity(patientDto);
        patient = patientService.update(patientId, patient);
        return ResponseEntity.ok().body(patientMapper.toDto(patient));
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID patientId) {
        patientService.delete(patientId);
        return ResponseEntity.noContent().build();
    }
}
