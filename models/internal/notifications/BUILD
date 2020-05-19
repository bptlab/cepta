load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_proto_grpc//github.com/grpc/grpc-web:defs.bzl", "commonjs_dts_grpc_compile")
load("@rules_proto_grpc//nodejs:defs.bzl", "nodejs_proto_compile")

proto_library(
    name = "notification_proto",
    srcs = ["notification.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal:delay_proto",
        "//models/internal/types:ids_proto",
        "//models/internal/types:coordinate_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "notification_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/notifications/notification",
    proto = ":notification_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal:delay_go_proto",
        "//models/internal/types:ids_go_proto",
        "//models/internal/types:coordinate_go_proto"
    ],
)

java_proto_library(
    name = "notification_java_proto",
    visibility = ["//visibility:public"],
    deps = [":notification_proto"],
)

commonjs_dts_grpc_compile(
    name = "notification_commonjs_dts_grpc_proto",
    deps = [":notification_proto"],
)

nodejs_proto_compile(
    name = "notification_nodejs_proto",
    deps = [":notification_proto"],
)

filegroup(
    name = "notifications",
    srcs = [
        ":notification_commonjs_dts_grpc_proto",
        ":notification_go_proto",
        ":notification_java_proto",
        ":notification_nodejs_proto",
    ],
    visibility = ["//visibility:public"],
)
