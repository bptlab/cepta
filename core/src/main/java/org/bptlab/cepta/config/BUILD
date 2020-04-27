load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:info_picocli_picocli",
        "@maven//:org_apache_kafka_kafka_clients",
    ],
)