load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")

go_binary(
    name = "authentication",
    embed = [":go_default_library"],
    data = [],
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go",
        "auth.go",
        "errors.go",
        "controllers.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/authentication",
    visibility = ["//visibility:private"],
    deps = [
        "//osiris/authentication/utils:go_default_library",
        "//osiris/authentication/models:go_default_library",
        "@com_github_dgrijalva_jwt_go//:go_default_library",
        "@com_github_gorilla_mux//:go_default_library",
        # "@com_github_shopify_sarama//:go_default_library",
        # "@com_github_urfave_cli_v2//:go_default_library",
        # "@com_github_sirupsen_logrus//:go_default_library",
    ],
)
