# Use a lightweight Java 25 base image (OpenJDK/Oracle)
FROM openjdk:25-slim

# Set the working directory
WORKDIR /app

# Copy the pre-built Fat JAR from your Gradle build into the container
# Ensure you build the JAR locally/in CI before pushing the release
COPY build/libs/github-tag-java-1.0.0.jar /app/action.jar

# Execute the application
ENTRYPOINT ["java", "-jar", "/app/action.jar"]