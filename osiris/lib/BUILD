load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")

go_library(
    name = "lib",
    srcs = [],
    importpath = "github.com/bptlab/cepta/osiris/lib",
    visibility = ["//visibility:public"],
    deps = [
        ":db",
        ":cli",
        ":utils",
        ":kafkaproducer",
        ":kafkaconsumer",
    ],
)

go_library(
    name = "db",
    srcs = [
        "db.go",
        "tmpmodels.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/db",
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)

go_library(
    name = "cli",
    srcs = [
        "cli.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/cli",
    visibility = ["//visibility:public"],
    deps = [],
)

go_library(
    name = "utils",
    srcs = [
        "utils.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/utils",
    visibility = ["//visibility:public"],
    deps = [],
)

go_library(
    name = "kafkaproducer",
    srcs = [
        "kafkaproducer.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/kafka/producer",
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)

go_library(
    name = "kafkaconsumer",
    srcs = [
        "kafkaconsumer.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/kafka/consumer",
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)
