load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")

go_binary(
    name = "traindataproducer",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "traindataproducer.go"
    ],
    importpath = "github.com/bptlab/cepta/aux/producers/traindataproducer",
    visibility = ["//visibility:private"],
    deps = [
        "//models/grpc:replayer_go_proto",
        "@org_golang_google_grpc//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)
