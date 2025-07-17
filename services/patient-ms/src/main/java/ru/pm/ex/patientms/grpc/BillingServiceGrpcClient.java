package ru.pm.ex.patientms.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import billing.BillingServiceGrpc.BillingServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BillingServiceGrpcClient {
    private final BillingServiceBlockingStub blockingStub;


    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}")
            String serviceAddress,
            @Value("${billing.service.grpc.port:9001}")
            int serverPort
    ) {
        log.info("Connecting to Billing gRPC Service at {}:{} ", serviceAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serviceAddress, serverPort)
                .usePlaintext()
                .build();

        this.blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String patientId, String name, String email) {
        var request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();

        var response = blockingStub.createBillingAccount(request);
        log.info("Received response from Billing gRPC service: {}", response);
        return response;
    }
}
