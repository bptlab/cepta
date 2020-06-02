# Osiris microservice backend

For help and to see CLI Options of the single microservice use --help:

```
bazel run //osiris/<microservice> -- --help
```

`<microservice>` is for example auth, notification, query or usermgmt 

### GRPC calls 

See `/models/grpc/<grpcserver>` for the possible calls you can make to the GRPC Servers

`<grpcserver>` is for example auth, notification, replayer or usermgmt 