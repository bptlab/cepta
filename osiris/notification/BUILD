load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")

go_binary(
    name = "notification",
    embed = [":go_default_library"],
    data = [],
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go",
        "kafkaconsumer.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/notification",
    visibility = ["//visibility:private"],
    deps = [
        "//osiris/notification/websocket:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
    ],
)
