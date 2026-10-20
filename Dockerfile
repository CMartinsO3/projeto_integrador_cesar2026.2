FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B verify

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

ARG GIT_SHA=local-dev
ARG APP_VERSION=0.0.1-SNAPSHOT

LABEL org.opencontainers.image.title="HemoFlow API" \
      org.opencontainers.image.description="Sistema academico de gestao e distribuicao de hemocomponentes" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${GIT_SHA}"

ENV GIT_SHA=${GIT_SHA} \
    APP_VERSION=${APP_VERSION} \
    PORT=8080

RUN groupadd --system --gid 1001 hemoflow && \
    useradd --system --uid 1001 --gid hemoflow --shell /sbin/nologin hemoflow

COPY --from=build --chown=hemoflow:hemoflow /app/target/*.jar /app/app.jar

USER hemoflow
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD curl -fsS http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]