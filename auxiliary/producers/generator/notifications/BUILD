load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")

go_binary(
    name = "notifications",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "generator.go",
    ],
    importpath = "github.com/bptlab/cepta/auxiliary/producers/generator/notifications",
    visibility = ["//visibility:private"],
    deps = [
        "//ci/versioning:go_default_library",
        "//models/constants:topic_go_proto",
        "//models/internal:delay_go_proto",
        "//models/internal/notifications:notification_go_proto",
        "//models/internal/types:ids_go_proto",
        "//models/internal/types:users_go_proto",
        "//osiris/lib:cli",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib/kafka/producer:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes/duration:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)
