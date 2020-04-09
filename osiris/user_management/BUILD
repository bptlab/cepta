load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library", "go_test")
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
    importpath = "github.com/bptlab/cepta/osiris/user_management",
    visibility = ["//visibility:private"],
    deps = [
        "//models/grpc:user_management_go_proto",
        "//models/grpc:authentication_go_proto",
        "//osiris/lib/db:go_default_library",
        "//osiris/authentication:go_default_library",
        "//osiris/lib:cli",
        "//ci/versioning:go_default_library",
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
    name = "server-test",
    srcs = ["server_test.go"],
    embed = [":go_default_library"],
    size = "small",
    tags = ["unit"],
)

test_suite(name = "smoke", tags = ["-docker", "-internal"])
test_suite(name = "unit", tags = ["unit", "-internal"])
test_suite(name = "integration", tags = ["integration", "-internal"])
test_suite(name = "internal")
