load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "event_proto",
    srcs = ["Event.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "event_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/event",
    proto = ":event_proto",
    visibility = ["//visibility:public"],
)

filegroup(
    name = "internal",
    srcs = [
        "//models/internal/updates",
        ":event_go_proto",
    ],
    visibility = ["//visibility:public"],
)