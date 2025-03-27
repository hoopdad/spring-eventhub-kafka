# Use an official Java runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the application JAR file
COPY target/event-hubs-kafka-java-producer-1.0-SNAPSHOT.jar app.jar
COPY src/main/resources/producer.config producer.config
# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
