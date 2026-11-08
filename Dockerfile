FROM eclipse-temurin:27-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY common/pom.xml common/pom.xml
COPY contracts/pom.xml contracts/pom.xml
COPY server-service/pom.xml server-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY common/src common/src
COPY contracts/src contracts/src
COPY server-service/src server-service/src
RUN ./mvnw -B -DskipTests -pl server-service -am package

FROM eclipse-temurin:27-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/server-service/target/server-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
