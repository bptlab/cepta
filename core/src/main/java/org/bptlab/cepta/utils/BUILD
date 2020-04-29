load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core/src/main/java/org/bptlab/cepta/utils/converters:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/triggers:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/types:java_default_library",
    ],
)