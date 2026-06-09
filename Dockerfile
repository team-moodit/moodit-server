# Build Stage
FROM bellsoft/liberica-openjdk-alpine:21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew build -x test --no-daemon

RUN java -Djarmode=tools -jar build/libs/*-SNAPSHOT.jar extract --layers --launcher --destination extracted

# Run stage
FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
