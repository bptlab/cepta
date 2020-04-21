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
        "//osiris/lib/kafka:consumer",
        "//osiris/lib/kafka:producer",
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
        "converters.go",
        "utils.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/utils",
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@org_golang_google_grpc//metadata:go_default_library",
        "@com_github_mitchellh_mapstructure//:go_default_library",
    ]
)
