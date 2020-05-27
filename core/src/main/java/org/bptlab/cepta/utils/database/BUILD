load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core/src/main/java/org/bptlab/cepta/config:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/database/mongohelper:java_default_library",
        "//models/events:planned_train_data_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_apache_commons_commons_text",
        "@maven//:org_javatuples_javatuples",
        "@maven//:org_mongodb_bson",
        "@maven//:org_mongodb_mongodb_driver_core",
        "@maven//:org_mongodb_mongodb_driver_reactivestreams",
        "@maven//:org_mongodb_mongodb_driver_sync",
    ],
)
