load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")
load("@rules_proto_grpc//github.com/grpc/grpc-web:defs.bzl", "commonjs_dts_grpc_compile")
load("@rules_proto_grpc//nodejs:defs.bzl", "nodejs_proto_compile")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")

# User management
proto_library(
    name = "usermgmt_proto",
    srcs = ["usermgmt.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:ids_proto",
        "//models/internal/types:result_proto",
        "//models/internal/types:transport_proto",
        "//models/internal/types:users_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "usermgmt_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/usermgmt",
    proto = ":usermgmt_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:ids_go_proto",
        "//models/internal/types:result_go_proto",
        "//models/internal/types:transport_go_proto",
        "//models/internal/types:users_go_proto",
    ],
)

commonjs_dts_grpc_compile(
    name = "usermgmt_commonjs_dts_grpc_proto",
    deps = [":usermgmt_proto"],
)

nodejs_proto_compile(
    name = "usermgmt_nodejs_proto",
    deps = [":usermgmt_proto"],
)

# Authentication

proto_library(
    name = "auth_proto",
    srcs = ["auth.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:users_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "auth_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/auth",
    proto = ":auth_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:users_go_proto",
    ],
)

commonjs_dts_grpc_compile(
    name = "auth_commonjs_dts_grpc_proto",
    deps = [":auth_proto"],
)

nodejs_proto_compile(
    name = "auth_nodejs_proto",
    deps = [":auth_proto"],
)

# Notification

proto_library(
    name = "notification_proto",
    srcs = ["notification.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:users_proto",
    ],
)

go_proto_library(
    name = "notification_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/notification",
    proto = ":notification_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:users_go_proto",
    ],
)

commonjs_dts_grpc_compile(
    name = "notification_commonjs_dts_grpc_proto",
    deps = [":notification_proto"],
)

nodejs_proto_compile(
    name = "notification_nodejs_proto",
    deps = [":notification_proto"],
)

# Replayer

proto_library(
    name = "replayer_proto",
    srcs = ["replayer.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/constants:topic_proto",
        "//models/events:event_proto",
        "//models/internal/types:result_proto",
        "@com_google_protobuf//:timestamp_proto",
        "@com_google_protobuf//:wrappers_proto",
    ],
)

go_proto_library(
    name = "replayer_go_grpc",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/replayer",
    proto = ":replayer_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/constants:topic_go_proto",
        "//models/events:event_go_proto",
        "//models/internal/types:result_go_proto",
    ],
)

java_proto_library(
    name = "replayer_java_proto",
    visibility = ["//visibility:public"],
    deps = [":replayer_proto"],
)

java_grpc_library(
    name = "replayer_java_grpc",
    srcs = [":replayer_proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":replayer_java_proto",
    ],
)

commonjs_dts_grpc_compile(
    name = "replayer_commonjs_dts_grpc_proto",
    deps = [":replayer_proto"],
)

nodejs_proto_compile(
    name = "replayer_nodejs_proto",
    deps = [":replayer_proto"],
)

filegroup(
    name = "grpc",
    srcs = [
        ":auth_commonjs_dts_grpc_proto",
        ":auth_go_proto",
        ":auth_nodejs_proto",
        ":notification_commonjs_dts_grpc_proto",
        ":notification_go_proto",
        ":notification_nodejs_proto",
        ":replayer_commonjs_dts_grpc_proto",
        ":replayer_go_grpc",
        ":replayer_java_proto",
        ":replayer_nodejs_proto",
        ":usermgmt_commonjs_dts_grpc_proto",
        ":usermgmt_go_proto",
        ":usermgmt_nodejs_proto",
    ],
    visibility = ["//visibility:public"],
)
