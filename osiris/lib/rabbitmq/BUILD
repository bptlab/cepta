load("@io_bazel_rules_go//go:def.bzl", "go_library", "go_binary")

go_binary(
    name = "rabbitmq-consumer",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_library(
    name = "consumer",
    srcs = [
        "consumer.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/rabbitmq/consumer",
    visibility = ["//visibility:public"],
    deps = [
        "//osiris/lib:cli",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_streadway_amqp//:go_default_library",
        "//models/events:train_delay_notification_go_proto",
    ],
)

go_binary(
    name = "rabbitmq-producer",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_library(
    name = "producer",
    srcs = [
        "producer.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/rabbitmq/producer",
    visibility = ["//visibility:public"],
    deps = [
        "//osiris/lib:cli",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_streadway_amqp//:go_default_library",
        "//models/events:train_delay_notification_go_proto",
    ],
)
