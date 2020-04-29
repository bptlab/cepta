#### Replayer

For Help use
```bash
bazel run //auxiliary/producers/replayer -- --help
```

For debugging, use
```bash
bazel run //auxiliary/producers/replayer -- --port 8080 --mode constant --replay-log debug
```

For your local mongoDB without authentication, use:
```bash
bazel run //auxiliary/producers/replayer -- --mongodb-user="" --mongodb-password="" --port 8080
```

When you want to replay only selected sources, use:
```bash
bazel run //auxiliary/producers/replayer -- --port 8080 --include-sources LIVE_TRAIN_DATA
```

When you want to replay immediately instead of waiting for the GRPC Call, use:
```bash
bazel run //auxiliary/producers/replayer -- --port 8080 --immediate true --include-sources LIVE_TRAIN_DATA
```