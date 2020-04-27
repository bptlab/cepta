load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "lib",
    srcs = [],
    importpath = "github.com/bptlab/cepta/osiris/lib",
    visibility = ["//visibility:public"],
    deps = [
        ":cli",
        "//osiris/lib/utils:go_default_library",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib/kafka/consumer:go_default_library",
        "//osiris/lib/kafka/producer:go_default_library",
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
        "//osiris/lib/utils:go_default_library",
        "@com_github_romnnn_flags4urfavecli//flags:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)

sub_tests = [
    "//osiris/lib/utils:test",
]

test_suite(
    name = "smoke",
    tags = [
        "-docker",
        "-internal",
    ],
    tests = sub_tests,
)

test_suite(
    name = "unit",
    tags = [
        "-internal",
        "unit",
    ],
    tests = sub_tests,
)

test_suite(
    name = "integration",
    tags = [
        "-internal",
        "integration",
    ],
    tests = sub_tests,
)

test_suite(
    name = "internal",
    tests = sub_tests,
)