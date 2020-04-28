load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun", "command")

go_binary(
    name = "notification",
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
  repository = "ceptaorg/notification",
  tag = "{STABLE_DOCKER_TAG}",
  visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/notification",
    visibility = ["//visibility:private"],
    x_defs = {
        "Version": "{STABLE_VERSION}",
        "BuildTime": "{buildDate}",
    },
    deps = [
        "//models/events:train_delay_notification_go_proto",
        "//osiris/notification/websocket:go_default_library",
        "//osiris/lib/kafka:consumer",
        "//ci/versioning:go_default_library",
        "//osiris/lib:cli",
        "//models/grpc:notification_go_proto",
        "//models/types:users_go_proto",
        "//osiris/lib/db:go_default_library",
        "//osiris/notification/lib:go_default_library",
        "//models/constants:constants_go_proto",
        "//osiris/lib/rabbitmq:consumer",
        "//osiris/lib/rabbitmq:producer",
        "@com_github_golang_protobuf//jsonpb:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@org_mongodb_go_mongo_driver//bson:go_default_library",
        "@org_mongodb_go_mongo_driver//bson/primitive:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
        "@com_github_romnnn_bsonpb//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
    ],
)

test_suite(name = "smoke", tags = ["-docker", "-internal"])
test_suite(name = "unit", tags = ["unit", "-internal"])
test_suite(name = "integration", tags = ["integration", "-internal"])
test_suite(name = "internal")
