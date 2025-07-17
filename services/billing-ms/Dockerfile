FROM gradle:8.4-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 4001
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "app.jar"]
