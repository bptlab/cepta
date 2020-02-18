FROM l.gcr.io/google/bazel AS protobuild
WORKDIR /app
COPY ./ /app
RUN ls -lia /app
RUN /app/anubis/gen-protos.sh echo "Compiled protobuf files"

FROM node:latest AS nodebuild
WORKDIR /anubis
ENV SKIPPROTOCOMPILATION 1
# RUN mkdir /anubis
COPY --from=protobuild /app/anubis /anubis 
RUN npm install && npm rebuild node-sass
RUN npm run build

FROM nginx:latest
EXPOSE 80
RUN mkdir /serve
COPY --from=nodebuild /anubis/dist/ /serve
COPY --from=nodebuild /anubis/nginx.conf /etc/nginx/nginx.conf
