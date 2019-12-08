FROM node:latest as build

WORKDIR /app

COPY ./ /app

RUN npm install && npm rebuild node-sass
RUN npm run build

FROM nginx:latest
EXPOSE 80
RUN mkdir /app
COPY --from=build /app/dist/ /app
COPY --from=build /app/nginx.conf /etc/nginx/nginx.conf
