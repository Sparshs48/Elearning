# Use a lightweight Java 17 runtime
FROM eclipse-temurin:17-jre-alpine

# Create and switch to /app
WORKDIR /app

# Copy the built JAR (from target/) into the image as app.jar
COPY target/*.jar app.jar

# Expose the port your Spring Boot app listens on (default 8888)
EXPOSE 8888

# Run the JAR (relative to WORKDIR)
ENTRYPOINT ["java","-jar","app.jar"]
