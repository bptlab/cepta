FROM maven:3.6.2-jdk-11-openj9 AS build
ARG MODULE
ENV MODULE_ENV=$MODULE
WORKDIR /app
COPY ./ /app
RUN apt-get update && apt-get install -y python wget unzip
RUN mvn -am -pl ${MODULE} clean package
RUN ls -lia /app/${MODULE}/target
RUN rm /app/${MODULE}/target/original-*.jar
RUN cp /app/${MODULE}/target/*.jar /test.jar

FROM gcr.io/distroless/java
COPY --from=build /test.jar /test.jar
ENTRYPOINT ["java", "-jar", "/test.jar"]