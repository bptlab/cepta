load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "command", "multirun")

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
    arguments = [
        "--norun",
    ],
    command = ":image",
)

multirun(
    name = "build-image",
    commands = [
        ":build-image-cmd",
    ],
    visibility = ["//visibility:public"],
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
        "server.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/notification",
    visibility = ["//visibility:private"],
    x_defs = {
        "Version": "{STABLE_VERSION}",
        "BuildTime": "{buildDate}",
    },
    deps = [
        "//ci/versioning:go_default_library",
        "//models/constants:topic_go_proto",
        "//models/internal/notifications:notification_go_proto",
        "//osiris/lib:cli",
        "//osiris/lib/kafka/consumer:go_default_library",
        "//osiris/notification/websocket:go_default_library",
        "@com_github_golang_protobuf//jsonpb:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)

test_suite(
    name = "smoke",
    tags = [
        "-docker",
        "-internal",
    ],
)

test_suite(
    name = "unit",
    tags = [
        "-internal",
        "unit",
    ],
)

test_suite(
    name = "integration",
    tags = [
        "-internal",
        "integration",
    ],
)

test_suite(name = "internal")
