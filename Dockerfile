# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-8 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
COPY public ./public

RUN mvn -DskipTests package dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:8-jre
WORKDIR /app

COPY --from=build /workspace/target/classes ./classes
COPY --from=build /workspace/target/dependency ./libs
COPY --from=build /workspace/public ./public

ENV PORT=8080
ENV HOST=0.0.0.0
EXPOSE 8080

CMD ["java", "-cp", "/app/classes:/app/libs/*", "edu.upc.dsa.Main"]

