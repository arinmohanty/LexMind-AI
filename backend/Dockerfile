# ---- Build stage: compile + package on JDK 21 (this is where the backend is compiled) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Cache dependencies first for faster rebuilds.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Skip tests in the image build (integration tests need Docker / run in CI).
RUN mvn -B -q -DskipTests clean package

# ---- Runtime stage: slim JRE ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Non-root user.
RUN groupadd --system lexmind && useradd --system --gid lexmind lexmind
COPY --from=build /build/target/*.jar app.jar
RUN mkdir -p /app/data/uploads && chown -R lexmind:lexmind /app
USER lexmind

EXPOSE 8080
ENV JAVA_OPTS=""
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=5 \
  CMD ["sh", "-c", "wget -qO- http://localhost:8080/actuator/health | grep -q UP || exit 1"]
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
