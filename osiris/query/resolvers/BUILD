load("@io_bazel_rules_go//go:def.bzl", "go_library")


go_library(
    name = "go_default_library",
    srcs = [
        "transport.go",
        "user.go",
        "root.go",
        "db.go",
        "scalars.go",
        "planned.go",
        "location.go",
        "coordinates.go",
        "eta.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/query/resolvers",
    visibility = ["//visibility:public"],
    deps = [
        "//osiris/lib/db:go_default_library",
        "//models/gql:query_go_proto",
        # "@com_github_friendsofgo_graphiql//:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        # "@com_github_graph_gophers_graphql_go//:go_default_library",
        # "@com_github_graph_gophers_graphql_go//relay:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        # "@com_github_urfave_cli_v2//:go_default_library",
        # "@com_github_pkg_errors//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
    ],
)
