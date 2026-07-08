# Build Stage
FROM bellsoft/liberica-openjdk-alpine:21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY clients/client-kakao/build.gradle clients/client-kakao/build.gradle
COPY core/core-api/build.gradle core/core-api/build.gradle

RUN ./gradlew :core:core-api:dependencies --no-daemon

COPY clients clients
COPY core/core-api core/core-api

RUN ./gradlew :core:core-api:build -x test --no-daemon

RUN java -Djarmode=tools -jar core/core-api/build/libs/*-SNAPSHOT.jar extract --layers --launcher --destination extracted

# Run stage
FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
