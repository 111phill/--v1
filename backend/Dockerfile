FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

RUN apk add --no-cache wget \
    && addgroup -S gradeapp && adduser -S gradeapp -G gradeapp \
    && mkdir -p /app/logs /app/uploads \
    && chown -R gradeapp:gradeapp /app

USER gradeapp

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=60s \
    CMD wget --spider -q http://localhost:8080/actuator/health || exit 1
