load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "air_proto",
    srcs = ["air.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "air_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/modalities/air",
    proto = ":air_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "air_java_proto",
    visibility = ["//visibility:public"],
    deps = [":air_proto"],
)
