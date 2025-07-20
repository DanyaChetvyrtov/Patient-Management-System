package ru.cs.ex.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LocalStack extends Stack {
    private final Vpc vpc;
    private final Cluster ecsCluster;

    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);

        this.vpc = createVpc();
        var authServiceDb = createDB("AuthServiceDB", "auth_db");
        var patientServiceDb = createDB("PatientServiceDB", "patient_db");

        var authDbHealthcheck = createHealthCheck(authServiceDb, "AuthServiceDBHealthCheck");
        var patientDbHealthcheck = createHealthCheck(patientServiceDb, "PatientServiceDBHealthCheck");

        var mskCluster = createMskCluster();
        this.ecsCluster = createEcsCluster();

        var authService = createFargateService(
                "AuthService",
                "auth-ms",
                List.of(4004),
                authServiceDb,
                Map.of("JWT_SECRET", "U0FVLUJSQVQyMjgtaXQnc1N1cGVyJGVDcmVUS0tLS0tleQ==")
        );

        authService.getNode().addDependency(authDbHealthcheck);
        authService.getNode().addDependency(authServiceDb);

        var billingService = createFargateService(
                "BillingService",
                "billing-ms",
                List.of(4001, 9001),
                null, null
        );

        var analyticsService = createFargateService(
                "AnalyticsService",
                "analytics-ms",
                List.of(4002),
                null, null
        );

        analyticsService.getNode().addDependency(mskCluster);


        var patientService = createFargateService(
                "PatientService",
                "patient-ms",
                List.of(4000),
                patientServiceDb,
                Map.of(
                        "BILLING_SERVICE_ADDRESS", "host.docker.internal",
                        "BILLING_SERVICE_PORT", "9001"
                )
        );

        patientService.getNode().addDependency(patientDbHealthcheck);
        patientService.getNode().addDependency(patientServiceDb);
        patientService.getNode().addDependency(billingService);
        patientService.getNode().addDependency(mskCluster);

        createApiGatewayService();
    }

    private Vpc createVpc() {
        return Vpc.Builder
                .create(this, "PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance createDB(String id, String dbName) {
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_16)
                                .build()
                ))
                .vpc(vpc)
                .instanceType(InstanceType.of(
                        InstanceClass.BURSTABLE2,
                        InstanceSize.MICRO
                ))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("postgres"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private CfnHealthCheck createHealthCheck(DatabaseInstance db, String id) {
        return CfnHealthCheck.Builder.create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
                .build();

    }

    private CfnCluster createMskCluster() {
        return CfnCluster.Builder.create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.8.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.xlarge")
                        .clientSubnets(
                                vpc.getPrivateSubnets()
                                        .stream().map(ISubnet::getSubnetId).toList())
                        .brokerAzDistribution("DEFAULT")
                        .build())
                .build();
    }

    private Cluster createEcsCluster() {
        return Cluster.Builder.create(this, "PatientManagementCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        .name("patient-management.local")
                        .build())
                .build();
    }

    private FargateService createFargateService(
            String id,
            String imageName,
            List<Integer> ports,
            DatabaseInstance db,
            Map<String, String> additionalEnvVars
    ) {
        var taskDefinition = FargateTaskDefinition.Builder.create(this, id + "Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        var containerOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(imageName))
                .portMappings(ports.stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                .logGroupName("/ecs/" + imageName)
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix(imageName)
                        .build()));

        var envVars = new HashMap<String, String>();
        envVars.put("SPRING_KAFKA_BOOTSTRAP", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");

        if (additionalEnvVars != null)
            envVars.putAll(additionalEnvVars);

        if (db != null) {
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s_db".formatted(
                    db.getDbInstanceEndpointAddress(), db.getDbInstanceEndpointPort(), imageName));
            envVars.put("SPRING_DATASOURCE_USERNAME", "postgres");
            envVars.put("SPRING_DATASOURCE_PASSWORD", db.getSecret().secretValueFromJson("password").toString());
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
            envVars.put("SPRING_JPA_HIBERNATE", "update");
        }

        containerOptions.environment(envVars);
        taskDefinition.addContainer(imageName + "Container", containerOptions.build());

        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .build();
    }

    private void createApiGatewayService() {
        var taskDefinition = FargateTaskDefinition.Builder.create(this, "ApiGatewayTaskDefinition")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        var containerOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("api-gateway"))
                .environment(
                        Map.of(
                                "SPRING_PROFILES_ACTIVE", "prod",
                                "AUTH_SERVICE_URL", "http://host.docker.internal:4004"
                                )
                )
                .portMappings(Stream.of(4003)
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                .logGroupName("/ecs/api-gateway")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix("api-gateway")
                        .build()));

        taskDefinition.addContainer("ApiGatewayContainer", containerOptions.build());

        var apiGateway = ApplicationLoadBalancedFargateService.Builder.create(this, "ApiGatewayService")
                .cluster(ecsCluster)
                .serviceName("api-gateway")
                .taskDefinition(taskDefinition)
                .desiredCount(1)
                .healthCheckGracePeriod(Duration.seconds(60))
                .build();
    }

    public static void main(final String[] args) {
        var app = new App(AppProps.builder().outdir("./cdk.out").build());
        var props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();

        new LocalStack(app, "localstack", props);
        app.synth();
        System.out.println("App synthesizing in progress...");
    }
}
