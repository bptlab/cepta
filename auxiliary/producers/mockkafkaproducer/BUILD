load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")

go_binary(
    name = "mockkafkaproducer",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "mockkafkaproducer.go"
    ],
    importpath = "github.com/bptlab/cepta/aux/producers/mockkafkaproducer",
    visibility = ["//visibility:private"],
    deps = [
        "//osiris/lib/db:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)