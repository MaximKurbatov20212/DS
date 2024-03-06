FROM openjdk:21

ARG JAR_FILE=build/*.jar

COPY ./build/libs/manager-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]

# docker build -t spring-manager .
# docker run spring-manager  здесь можно ещё порты прокинуть

