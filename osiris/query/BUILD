load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")

go_binary(
    name = "query",
    embed = [":go_default_library"],
    data = [
        "//models/gql:query_gql_proto",
    ],
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/query",
    visibility = ["//visibility:private"],
    deps = [
        "//osiris/lib:db",
        "//models/gql:query_go_proto",
        "//osiris/query/resolvers:go_default_library",
        "@com_github_friendsofgo_graphiql//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_graph_gophers_graphql_go//:go_default_library",
        "@com_github_graph_gophers_graphql_go//relay:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_pkg_errors//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
    ],
)
