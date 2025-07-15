package ru.pm.ex.patientms.mapper;

import org.mapstruct.Mapper;
import ru.pm.ex.patientms.dto.PatientDto;
import ru.pm.ex.patientms.model.Patient;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientDto toDto(Patient account);

    Patient toEntity(PatientDto accountDto);
}
