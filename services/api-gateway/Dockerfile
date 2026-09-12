# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/

RUN ./mvnw -q -DskipTests package \
    && JAR="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)" \
    && test -n "$JAR" \
    && cp "$JAR" /tmp/app.jar


FROM eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system cloudbank \
    && useradd \
         --system \
         --gid cloudbank \
         --home-dir /app \
         --shell /usr/sbin/nologin \
         cloudbank

WORKDIR /app

COPY --from=build \
     --chown=cloudbank:cloudbank \
     /tmp/app.jar \
     /app/app.jar

USER cloudbank

EXPOSE 8082

HEALTHCHECK \
  --interval=30s \
  --timeout=5s \
  --start-period=30s \
  --retries=3 \
  CMD curl --fail --silent \
      http://127.0.0.1:8082/actuator/health \
      >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
