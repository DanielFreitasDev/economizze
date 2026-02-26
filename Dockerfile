# Etapa de build da aplicacao Spring Boot
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN ./mvnw -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -DskipTests clean package

# Etapa final de runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV TZ=America/Sao_Paulo
ENV JAVA_OPTS=""

COPY --from=build /app/target/economizze-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
