load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")

go_binary(
    name = "query",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "db.go",
        "query.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/query",
    visibility = ["//visibility:private"],
    deps = [
        "//osiris/lib:db",
        "@com_github_friendsofgo_graphiql//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_graph_gophers_graphql_go//:go_default_library",
        "@com_github_graph_gophers_graphql_go//relay:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)
