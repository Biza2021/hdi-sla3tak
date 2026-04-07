FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 1001 --create-home appuser \
    && mkdir -p /app/data /app/uploads/items \
    && chown -R appuser:appuser /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
