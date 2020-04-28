load("@rules_proto//proto:defs.bzl", "proto_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")

proto_library(
    name = "constants_proto",
    srcs = ["constants.proto"],
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "constants_java_proto",
    deps = [":constants_proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "constants_go_proto",
    importpath = "github.com/bptlab/cepta/models/constants",
    proto = ":constants_proto",
    visibility = ["//visibility:public"],
)

filegroup(
    name = "constants",
    srcs = [
        ":constants_go_proto", ":constants_java_proto",
    ],
    visibility = ["//visibility:public"],
)
