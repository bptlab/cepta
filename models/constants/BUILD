load("@rules_proto//proto:defs.bzl", "proto_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")

proto_library(
    name = "topic_proto",
    srcs = ["Topic.proto"],
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "topic_java_proto",
    visibility = ["//visibility:public"],
    deps = [":topic_proto"],
)

go_proto_library(
    name = "topic_go_proto",
    importpath = "github.com/bptlab/cepta/models/constants/topic",
    proto = ":topic_proto",
    visibility = ["//visibility:public"],
)

filegroup(
    name = "constants",
    srcs = [
        ":topic_go_proto",
        ":topic_java_proto",
    ],
    visibility = ["//visibility:public"],
)
