#FROM alpine:3.19.1
#
#WORKDIR /worker
#
#RUN apk update
#RUN apk add openjdk21-jre
#
#COPY . .
#
#EXPOSE 8080
#
#CMD sh ./gradlew build
#CMD sh ./gradlew build && java -jar ./build/libs/worker-0.0.1-SNAPSHOT.jar

FROM openjdk:21

ARG JAR_FILE=build/*.jar

COPY ./build/libs/worker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]

