load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun", "command")

go_binary(
    name = "query",
    embed = [":go_default_library"],
    data = [
        "//models/gql:query_gql_proto",
    ],
    visibility = ["//visibility:public"],
)

go_image(
    name = "image",
    embed = [":go_default_library"],
    data = [
        "//models/gql:query_gql_proto",
    ],
    visibility = ["//visibility:public"],
)

command(
    name = "build-image-cmd",
    command = ":image",
    arguments = [
        "--norun"
    ]
)

multirun(
    visibility = ["//visibility:public"],
    name = "build-image",
    commands = [
        ":build-image-cmd",
    ],
)

container_push(
  name = "publish",
  format = "Docker",
  image = ":image",
  registry = "index.docker.io",
  repository = "ceptaorg/query",
  tag = "{STABLE_DOCKER_TAG}",
  visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go",
    ],
    x_defs = {
        "Version": "{STABLE_VERSION}",
        "BuildTime": "{buildDate}",
    },
    importpath = "github.com/bptlab/cepta/osiris/query",
    visibility = ["//visibility:private"],
    deps = [
        "//osiris/lib:cli",
        "//osiris/lib/db:go_default_library",
        "//models/gql:query_go_proto",
        "//ci/versioning:go_default_library",
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

test_suite(name = "smoke", tags = ["-docker", "-internal"])
test_suite(name = "unit", tags = ["unit", "-internal"])
test_suite(name = "integration", tags = ["integration", "-internal"])
test_suite(name = "internal")
