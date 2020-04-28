load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "maritime_proto",
    srcs = ["maritime.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "maritime_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/modalities/maritime",
    proto = ":maritime_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "maritime_java_proto",
    visibility = ["//visibility:public"],
    deps = [":maritime_proto"],
)
