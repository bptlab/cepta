load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "update_proto",
    srcs = ["Update.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "update_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/updates/update",
    proto = ":update_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "position_update_proto",
    srcs = ["PositionUpdate.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:coordinate_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "position_update_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/updates/position",
    proto = ":position_update_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:coordinate_go_proto",
    ],
)

filegroup(
    name = "updates",
    srcs = [
        ":position_update_go_proto",
        ":update_go_proto",
    ],
    visibility = ["//visibility:public"],
)
