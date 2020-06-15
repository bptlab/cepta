load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
load("@rules_proto_grpc//github.com/grpc/grpc-web:defs.bzl", "ts_grpc_compile")
load("@rules_proto_grpc//nodejs:defs.bzl", "nodejs_proto_compile")

proto_library(
    name = "monitor_proto",
    srcs = ["monitor.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/events:count_of_trains_at_station_event_proto",
        "//models/events:event_proto",
        "//models/events:no_matching_planned_train_data_event_proto",
        "//models/events:stays_in_station_event_proto",
        "//models/internal/notifications:notification_proto",
    ],
)

java_proto_library(
    name = "monitor_java_proto",
    visibility = ["//visibility:public"],
    deps = [":monitor_proto"],
)

nodejs_proto_compile(
    name = "monitor_nodejs_proto",
    deps = [":monitor_proto"],
)

filegroup(
    name = "monitoring",
    srcs = [
        ":monitor_java_proto",
        ":monitor_nodejs_proto",
    ],
    visibility = ["//visibility:public"],
)
