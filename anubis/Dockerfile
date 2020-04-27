ARG MONITORING_URL
FROM l.gcr.io/google/bazel AS protobuild
ARG MONITORING_URL
WORKDIR /app
COPY ./ /app
RUN ls -lia /app
RUN /app/anubis/gen-protos.sh echo "Compiled protobuf files"

FROM node:latest AS nodebuild
ARG MONITORING_URL
WORKDIR /anubis
ENV SKIPPROTOCOMPILATION 1
ENV MONITORING_URL=${MONITORING_URL}
COPY --from=protobuild /app/anubis /anubis
RUN ls -lia /anubis
RUN echo $MONITORING_URL
RUN npm install && npm rebuild node-sass
RUN npm run build
RUN ls -lia /anubis/dist/

FROM nginx:latest
EXPOSE 80
COPY --from=nodebuild /anubis/dist /serve
RUN ls -lia /serve
COPY --from=nodebuild /anubis/nginx.conf /etc/nginx/nginx.conf
