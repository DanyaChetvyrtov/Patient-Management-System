plugins {
    id("java")
}

group = "ru.cs.ex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("io.rest-assured:rest-assured-bom:5.5.5"))
    testImplementation("io.rest-assured:rest-assured")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}