FROM eclipse-temurin:17-alpine AS base

WORKDIR /app

COPY src src
COPY .mvn .mvn
COPY pom.xml mvnw ./

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:resolve

FROM base AS test

WORKDIR /app

ENTRYPOINT ["./mvnw", "test"]

FROM base AS dev

WORKDIR /app

ENTRYPOINT ["./mvnw", "spring-boot:run"]

FROM eclipse-temurin:17-alpine AS builder

WORKDIR /build

COPY .mvn .mvn
COPY pom.xml mvnw ./
COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -Pproduction

RUN $JAVA_HOME/bin/jlink \
    --module-path $JAVA_HOME/jmods \
    --add-modules ALL-MODULE-PATH \
    --output /jre \
    --compress=2 \
    --no-header-files \
    --no-man-pages

FROM alpine:latest AS prod

WORKDIR /src

ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=builder /jre $JAVA_HOME

COPY --from=builder /build/target/*.jar /src/app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=production"]

