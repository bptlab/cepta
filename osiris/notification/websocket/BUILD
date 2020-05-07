load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "client.go",
        "pool.go",
        "websocket.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/notification/websocket",
    visibility = ["//visibility:public"],
    deps = [
        "//models/grpc:notification_go_proto",
        "//models/internal/notifications:notification_go_proto",
        "//models/internal/types:users_go_proto",
        "//osiris/lib/rabbitmq/producer:go_default_library",
        "@com_github_go_redis_redis//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_gorilla_websocket//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
    ],
)
