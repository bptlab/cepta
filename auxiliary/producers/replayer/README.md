#### Replayer

For debugging, use
```bash
bazel run //auxiliary/producers/replayer -- --port 8080 --mode constant --log debug
```

For your local mongoDB without authentication, use:
```bash
bazel run //auxiliary/producers/replayer -- --mongodb-user="" --mongodb-password="" --port 8080
```

When you want to replay only selected sources, use:
```bash
bazel run //auxiliary/producers/replayer -- --port 8080 --include-sources "livetraindata, plannedtraindata"
```

When you want to replay immediately without waiting for GRPC Call, use:
```bash
bazel run //auxiliary/producers/replayer -- --port 8080 --immediate
```