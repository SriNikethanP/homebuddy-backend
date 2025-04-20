FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Verify the JAR file exists and list its name
RUN ls -l target/*.jar

EXPOSE 8080

# Use the specific JAR file name
ENTRYPOINT ["sh", "-c", "java -jar target/backend-0.0.1-SNAPSHOT.jar"] 