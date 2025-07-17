import java.util.*

plugins {
	java
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.protobuf") version "0.9.4"
}

group = "ru.cs.ex"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
val osClassifier = when {
	osName.contains("win") -> "windows"
	osName.contains("linux") -> "linux"
	osName.contains("mac") -> "osx"
	else -> throw GradleException("Unknown OS: $osName")
}

val protobufVersion = "4.29.1"
val grpcVersion = "1.73.0"
val protocVersion = "3.25.5" // Версия protoc компилятора

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2025.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")

	// cloud
	implementation("org.springframework.cloud:spring-cloud-starter-config")

	// gRPC
	implementation("io.grpc:grpc-stub:$grpcVersion")
	implementation("io.grpc:grpc-protobuf:$grpcVersion")
	implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
	implementation("org.apache.tomcat:annotations-api:6.0.53")
	implementation("net.devh:grpc-spring-boot-starter:3.1.0.RELEASE")
	implementation("com.google.protobuf:protobuf-java:$protobufVersion")

	// lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:$protocVersion:${osClassifier}-x86_64"
	}
	plugins {
		create("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion:${osClassifier}-x86_64"
		}
	}
	generateProtoTasks {
		ofSourceSet("main").forEach {
			it.plugins {
				create("grpc")
			}
		}
	}
}


// Для корректной работы osdetector
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("kr.motd.maven:os-maven-plugin:1.7.0")
	}
}