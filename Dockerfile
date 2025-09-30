# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Ensure wrapper is executable in case host (e.g. Windows) dropped exec bit
RUN chmod +x mvnw || true
RUN ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
# non-root
RUN addgroup --system app && adduser --system --ingroup app app
USER app
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
# Render provides $PORT; pass it through
CMD ["sh","-c","java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]

