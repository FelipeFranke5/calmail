FROM openjdk:21
COPY . .
CMD ["java", "-jar", "/target/calmail-0.0.1.jar"]
