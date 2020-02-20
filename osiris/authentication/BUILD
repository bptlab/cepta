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
    ],
)

go_test(
    name = "go_default_test",
    srcs = ["controllers_test.go"],
    embed = [":go_default_library"],
    size = "medium"
)

test_suite(
    name = "all",
    tests = [
        "//osiris/authentication:go_default_test",
        "//osiris/authentication/models:go_default_test../"
    ],
)