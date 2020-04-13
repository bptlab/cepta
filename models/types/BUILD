load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "coordinate_proto",
    srcs = ["Coordinate.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "coordinate_go_proto",
    importpath = "github.com/bptlab/cepta/models/types/coordinate",
    proto = ":coordinate_proto",
    visibility = ["//visibility:public"],
)