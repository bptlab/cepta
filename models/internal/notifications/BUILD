load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "notification_proto",
    srcs = ["notification.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal:delay_proto",
        "//models/internal/types:ids_proto",
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
    ],
)

java_proto_library(
    name = "notification_java_proto",
    visibility = ["//visibility:public"],
    deps = [":notification_proto"],
)
