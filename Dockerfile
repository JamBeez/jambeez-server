FROM maven:3-eclipse-temurin-17 as builder

WORKDIR /usr/src/jambeez
COPY src src
COPY pom.xml pom.xml
RUN mvn clean package

FROM eclipse-temurin:18
ENV TZ=Europe/Berlin

WORKDIR /usr/src/jambeez
COPY --from=builder /usr/src/jambeez/target/server-*.jar jambeez-server.jar
EXPOSE 8080
ENTRYPOINT java -jar /usr/src/jambeez/jambeez-server.jar
