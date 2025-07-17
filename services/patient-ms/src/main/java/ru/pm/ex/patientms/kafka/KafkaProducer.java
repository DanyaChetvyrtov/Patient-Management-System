package ru.pm.ex.patientms.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import patient.events.PatientEvent;
import ru.pm.ex.patientms.model.Patient;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendEvent(Patient patient) {
        var event = PatientEvent.newBuilder()
                .setPatientId(patient.getPatientId().toString())
                .setName(patient.getName())
                .setName(patient.getEmail())
                .setEventType("PATIENT_CREATED")
                .build();

        try {
            kafkaTemplate.send("patient", event.toByteArray());
        } catch (Exception ex) {
            log.error("Error while sending event: {}", event, ex);
        }
    }
}
