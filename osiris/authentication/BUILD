load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library", "go_test")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")

go_binary(
    name = "authentication",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_image(
    name = "image",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

container_push(
  name = "publish",
  format = "Docker",
  image = ":image",
  registry = "index.docker.io",
  repository = "ceptaorg/authentication",
  tag = "master",
  visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/authentication",
    visibility = ["//visibility:private"],
    deps = [
        "//models/grpc:authentication_go_proto",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib:cli",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_dgrijalva_jwt_go//:go_default_library",
        "@com_github_gorilla_mux//:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_lib_pq//:go_default_library",
        "@com_github_selvatico_go_mocket//:go_default_library",
        "@com_github_grpc_grpc_go//test/bufconn:go_default_library"
        
    ],
)

go_test(
    name = "go_default_test",
    srcs = ["server_test.go"],
    embed = [":go_default_library"],
    size = "medium"
)
test_suite(
    name = "all_tests",
    tests = [
        "//osiris/authentication:go_default_test",
        # "//osiris/user_management/models:go_default_test"
    ]
)