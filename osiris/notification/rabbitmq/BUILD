load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "emit_to_queue.go",
        "receive_from_queue.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/notification/rabbitmq",
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_streadadway_amqp//:go_default_library",
    ],
)
