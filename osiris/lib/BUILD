load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "lib",
    srcs = [],
    importpath = "github.com/bptlab/cepta/osiris/lib",
    visibility = ["//visibility:public"],
    deps = [
        ":cli",
        ":utils",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib/kafka:producer",
        "//osiris/lib/kafka:consumer",
    ],
)

go_library(
    name = "cli",
    srcs = [
        "options.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/cli",
    visibility = ["//visibility:public"],
    deps = [
        ":utils",
        "@com_github_romnnn_flags4urfavecli//flags:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)

go_library(
    name = "utils",
    srcs = [
        "utils.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/utils",
    visibility = ["//visibility:public"],
)
