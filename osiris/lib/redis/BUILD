load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "redis.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/redis",
    visibility = ["//visibility:public"],
    deps = [
        "//osiris/lib:cli",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)
