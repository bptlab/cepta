load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:org_mongodb_bson",
        "@maven//:org_mongodb_mongodb_driver_core",
        "@maven//:org_mongodb_mongodb_driver_reactivestreams",
        "@maven//:org_reactivestreams_reactive_streams",
    ],
)
