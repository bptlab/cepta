load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "client.go",
        "pool.go",
        "websocket.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/notification/websocket",
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_gorilla_websocket//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
    ],
)
