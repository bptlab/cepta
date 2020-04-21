load("@io_bazel_rules_go//go:def.bzl", "go_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "basic_proto",
    srcs = ["basic.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "basic_go_proto",
    importpath = "github.com/bptlab/cepta/models/types/basic",
    proto = ":basic_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "result_proto",
    srcs = ["result.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "result_go_proto",
    importpath = "github.com/bptlab/cepta/models/types/result",
    proto = ":result_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "transports_proto",
    srcs = ["transports.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "transports_go_proto",
    importpath = "github.com/bptlab/cepta/models/types/transports",
    proto = ":transports_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "users_proto",
    srcs = ["users.proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":transports_proto"
    ],
)

go_proto_library(
    name = "users_go_proto",
    importpath = "github.com/bptlab/cepta/models/types/users",
    proto = ":users_proto",
    visibility = ["//visibility:public"],
    deps = [
        ":transports_go_proto"
    ],
)
