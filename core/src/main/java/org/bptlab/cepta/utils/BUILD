load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_javatuples_javatuples",
    ],
)