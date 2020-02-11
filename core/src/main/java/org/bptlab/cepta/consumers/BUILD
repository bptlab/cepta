load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "consumers",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:org_apache_kafka_kafka_clients",
        "@maven//:info_picocli_picocli",
        "//models/events:live_train_data_java_proto",
        "//core/src/main/java/org/bptlab/cepta/config:config",
    ],
)