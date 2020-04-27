load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
    ],
)