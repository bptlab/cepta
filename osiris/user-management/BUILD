load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")

go_binary(
    name = "user-management",
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
  repository = "ceptaorg/user-management",
  tag = "master",
  visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/user-management",
    visibility = ["//visibility:private"],
    deps = [
        # "//osiris/user-management/utils:go_default_library",
        # "//osiris/user-management/models:go_default_library",
        # "@com_github_dgrijalva_jwt_go//:go_default_library",
        "@com_github_gorilla_mux//:go_default_library",
    ],
)
