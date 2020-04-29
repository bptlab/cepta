load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "producer.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/rabbitmq/producer",
    visibility = ["//visibility:public"],
    deps = [
        "//osiris/lib:cli",
        "//osiris/lib/rabbitmq:go_default_library",
        "@com_github_streadway_amqp//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)
