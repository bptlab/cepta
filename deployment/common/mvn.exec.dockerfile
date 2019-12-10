FROM maven:3.6.2-jdk-11-openj9
ARG MODULE
ENV MODULE_ENV=$MODULE
WORKDIR /app
COPY ./ /app
RUN apt-get update && apt-get install -y python wget unzip
RUN mvn -pl ${MODULE} -am clean install
RUN ls -lia /app/${MODULE}/target
ENTRYPOINT ["/bin/sh", "deployment/common/mvn.entrypoint.sh"]