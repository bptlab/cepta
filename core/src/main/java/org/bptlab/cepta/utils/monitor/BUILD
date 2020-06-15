load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core/src/main/java/org/bptlab/cepta/config:java_default_library",
        "//models/events:planned_train_data_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_apache_commons_commons_text",
        "@maven//:org_apache_kafka_kafka_clients",
        "@maven//:org_javatuples_javatuples",
    ],
)
