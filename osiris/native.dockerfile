FROM golang:latest

# Copy the app's source files
# ENV SRC_DIR=/service/query/
# COPY . $SRC_DIR
# WORKDIR $SRC_DIR

ENV SRC_DIR=/osiris/query/
WORKDIR /app
COPY ${SRC_DIR} /app/
COPY go.* /app/
RUN ls -lia /app

# RUN go get -v github.com/jteeuwen/go-bindata/...
# RUN export PATH=$PATH:$GOPATH/bin
# RUN go generate ./schema
# RUN go get -v ./
EXPOSE 80
RUN go build -o app
ENTRYPOINT [ "app" ]
# ENTRYPOINT ["./go-graphql-starter"]