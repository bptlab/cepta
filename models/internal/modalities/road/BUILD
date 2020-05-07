load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "road_proto",
    srcs = ["road.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "road_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/modalities/road",
    proto = ":road_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "road_java_proto",
    visibility = ["//visibility:public"],
    deps = [":road_proto"],
)
