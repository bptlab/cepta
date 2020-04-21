load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library", "go_test")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun", "command")

go_binary(
    name = "auth",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_image(
    name = "image",
    embed = [":go_default_library"],
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
  repository = "ceptaorg/auth",
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
    importpath = "github.com/bptlab/cepta/osiris/auth",
    visibility = ["//visibility:public"],
    deps = [
        "//models/grpc:auth_go_proto",
        "//models/types:users_go_proto",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib:cli",
        "//osiris/auth/lib:go_default_library",
        "//ci/versioning:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_google_uuid//:go_default_library",
        "@org_mongodb_go_mongo_driver//bson:go_default_library",
        "@org_mongodb_go_mongo_driver//bson/primitive:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
        "@com_github_romnnn_bsonpb//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@org_golang_google_grpc//status:go_default_library",
        "@org_golang_google_grpc//codes:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_dgrijalva_jwt_go//:go_default_library",
        "@com_github_gorilla_mux//:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_lib_pq//:go_default_library",
        "@com_github_selvatico_go_mocket//:go_default_library",
        "@com_github_grpc_grpc_go//test/bufconn:go_default_library",
        "@com_github_lestrrat_go_jwx//jwk:go_default_library",
    ],
)
go_test(
    name = "server-test",
    srcs = ["server_test.go"],
    embed = [":go_default_library"],
    size = "medium",
    tags = ["integration", "docker"],
    deps = [
        "//osiris/lib/testing:go_default_library",
        "//models/grpc:usermgmt_go_proto",
        "//osiris/usermgmt:go_default_library",
        "@com_github_testcontainers_testcontainers_go//:go_default_library",
    ]
)

test_suite(name = "smoke", tags = ["-docker", "-internal"])
test_suite(name = "unit", tags = ["unit", "-internal"])
test_suite(name = "integration", tags = ["integration", "-internal"])
test_suite(name = "internal")