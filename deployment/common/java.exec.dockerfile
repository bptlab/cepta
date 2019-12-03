FROM maven:3.6.2-jdk-11-openj9
ARG MODULE
ENV MODULE_ENV=$MODULE
WORKDIR /app
COPY ./ /app
RUN echo mvn -pl ${MODULE} -am clean install
RUN mvn -pl ${MODULE} -am clean install
RUN ls -lia /app/${MODULE}/target
# TODO: Fix error with appending cli options after entrypoint
ENTRYPOINT ["sh", "-c", "mvn exec:java -pl $MODULE_ENV -am"]