```bash
bazel run //:build-images
bazel test //core/src/test/java/org/bptlab/cepta/populate-station-mapping --test_output streamed --cache_test_results=no
```