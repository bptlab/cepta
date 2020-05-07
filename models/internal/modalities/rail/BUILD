load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "rail_proto",
    srcs = ["rail.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "rail_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/modalities/rail",
    proto = ":rail_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "rail_java_proto",
    visibility = ["//visibility:public"],
    deps = [":rail_proto"],
)
