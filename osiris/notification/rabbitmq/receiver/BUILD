load("@io_bazel_rules_go//go:def.bzl", "go_library", "go_binary")

go_binary(
    name = "rabbitmq-receiver",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "receive_from_queue.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/notification/rabbitmq",
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_streadway_amqp//:go_default_library",
        "//models/events:train_delay_notification_go_proto",
    ],
)
