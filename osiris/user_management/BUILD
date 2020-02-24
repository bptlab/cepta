load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")

go_binary(
    name = "user_management",
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
  repository = "ceptaorg/user_management",
  tag = "master",
  visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go",
        "user_management.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/user_management",
    visibility = ["//visibility:private"],
    deps = [
        "//models/grpc:user_management_go_proto",
        "//osiris/lib:db",
        "//osiris/lib:cli",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_dgrijalva_jwt_go//:go_default_library",
        "@com_github_gorilla_mux//:go_default_library",
    ],
)
