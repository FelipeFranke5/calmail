FROM sapmachine:23.0.2-jdk-alpine
COPY . .
COPY ai_role_pt.txt /target/ai_role_pt.txt
CMD ["java", "-jar", "/target/calmail-0.0.1.jar"]