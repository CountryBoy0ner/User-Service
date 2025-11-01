FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar

ENV JAVA_OPTS=""

ENV SERVER_PORT=8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
