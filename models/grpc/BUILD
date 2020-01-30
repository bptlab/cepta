load("@io_bazel_rules_go//go:def.bzl", "go_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto_grpc//github.com/grpc/grpc-web:defs.bzl", "ts_grpc_compile")

proto_library(
    name = "replayer_proto",
    srcs = ["replayer.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

go_proto_library(
    name = "replayer_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/replayer",
    proto = ":replayer_proto",
    visibility = ["//visibility:public"],
)

ts_grpc_compile(
    name = "replayer_ts_grpc_proto",
    deps = [":replayer_proto"],
)

go_library(
    name = "go_default_library",
    embed = [":replayer_go_proto"],
    importpath = "github.com/bptlab/cepta/models/grpc/replayer",
    visibility = ["//visibility:public"],
)
