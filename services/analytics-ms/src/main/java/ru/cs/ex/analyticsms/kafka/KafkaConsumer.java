package ru.cs.ex.analyticsms.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import patient.events.PatientEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    @KafkaListener(topics = "patient", groupId = "analytics-ms")
    public void consumeEvent(byte[] event) {
        try {
            var patientEvent = PatientEvent.parseFrom(event);
            log.info("Patient event received: patientId={}, patientName={}, patientEmail={}",
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail());
            // perform any business logic

        } catch (Exception ex) {
            log.error("Error parsing event", ex);
        }
    }
}
