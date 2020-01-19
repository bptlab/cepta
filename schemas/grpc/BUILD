load("@io_bazel_rules_go//go:def.bzl", "go_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")

proto_library(
    name = "replayer_proto",
    srcs = ["replayer.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "replayer_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/schemas/service/replayer",
    proto = ":replayer_proto",
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    embed = [":replayer_go_proto"],
    importpath = "github.com/bptlab/cepta/schemas/service/replayer",
    visibility = ["//visibility:public"],
)
