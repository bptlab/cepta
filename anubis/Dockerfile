FROM l.gcr.io/google/bazel AS protobuild
WORKDIR /app
COPY ./ /app
RUN ./gen-protos.sh echo "Compiled protobuf files"

FROM node:latest AS nodebuild
ENV SKIPPROTOCOMPILATION 1
RUN mkdir /app
COPY --from=protobuild /app/ /app 
RUN npm install && npm rebuild node-sass
RUN npm run build

FROM nginx:latest
EXPOSE 80
RUN mkdir /app
COPY --from=nodebuild /app/dist/ /app
COPY --from=nodebuild /app/nginx.conf /etc/nginx/nginx.conf
