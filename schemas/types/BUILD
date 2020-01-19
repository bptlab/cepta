load("@io_bazel_rules_go//go:def.bzl", "go_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")

proto_library(
    name = "basic_proto",
    srcs = ["basic.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "basic_go_proto",
    importpath = "github.com/bptlab/cepta/schemas/types/basic",
    proto = ":basic_proto",
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    embed = [":basic_go_proto"],
    importpath = "github.com/bptlab/cepta/schemas/types/basic",
    visibility = ["//visibility:public"],
)
