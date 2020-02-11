load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "lib",
    srcs = [],
    importpath = "github.com/bptlab/cepta/osiris/lib",
    visibility = ["//visibility:public"],
    deps = [
        ":db",
        ":cli",
        ":utils",
        "//osiris/lib/kafka:producer",
        "//osiris/lib/kafka:consumer",
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
        ":cli",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)

go_library(
    name = "cli",
    srcs = [
        "cli.go",
        "options.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/cli",
    visibility = ["//visibility:public"],
    deps = [
        ":utils",
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
