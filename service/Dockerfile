FROM golang:latest

ENV SRC_DIR=/go/src/github.com/OscarYuen/go-graphql-starter/
COPY . $SRC_DIR
WORKDIR $SRC_DIR

# Build it:
RUN go get -v github.com/jteeuwen/go-bindata/...
RUN export PATH=$PATH:$GOPATH/bin
RUN go generate ./schema
RUN go get -v ./
RUN go build
ENTRYPOINT ["./go-graphql-starter"]
EXPOSE 3000