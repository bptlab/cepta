load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library", "go_test")
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
        "//models/grpc:notification_go_proto",
        "//models/grpc:usermgmt_go_proto",
        "//models/internal:delay_go_proto",
        "//models/internal/notifications:notification_go_proto",
        "//models/internal/types:ids_go_proto",
        "//models/internal/types:result_go_proto",
        "//models/internal/types:transport_go_proto",
        "//models/internal/types:users_go_proto",
        "//osiris/lib:cli",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib/kafka/consumer:go_default_library",
        "//osiris/lib/kafka/producer:go_default_library",
        "//osiris/lib/rabbitmq/consumer:go_default_library",
        "//osiris/lib/rabbitmq/producer:go_default_library",
        "//osiris/lib/redis:go_default_library",
        "//osiris/lib/utils:go_default_library",
        "//osiris/notification/websocket:go_default_library",
        "//osiris/usermgmt:go_default_library",
        "@com_github_go_redis_redis//:go_default_library",
        "@com_github_golang_protobuf//jsonpb:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes/duration:go_default_library",
        "@com_github_hashicorp_golang_lru//:go_default_library",
        "@com_github_pkg_errors//:go_default_library",
        "@com_github_romnnn_bsonpb//:go_default_library",
        "@com_github_romnnn_flags4urfavecli//values:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@org_mongodb_go_mongo_driver//bson:go_default_library",
        "@org_mongodb_go_mongo_driver//bson/primitive:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
    ],
)

go_test(
    name = "server-test",
    size = "medium",
    srcs = [
        "tests/assertions.go",
        "tests/helpers.go",
        "tests/provider.go",
        "tests/server_test.go",
    ],
    embed = [":go_default_library"],
    tags = [
        "docker",
        "integration",
    ],
    deps = [
        "//models/grpc:notification_go_proto",
        "//models/grpc:usermgmt_go_proto",
        "//osiris/lib/kafka:go_default_library",
        "//osiris/lib/rabbitmq:go_default_library",
        "//osiris/lib/redis:go_default_library",
        "//osiris/usermgmt:go_default_library",
        "@com_github_davecgh_go_spew//spew:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_gorilla_websocket//:go_default_library",
        "@com_github_romnnn_deepequal//:go_default_library",
        "@com_github_romnnn_testcontainers//:go_default_library",
        "@com_github_romnnn_testcontainers//kafka:go_default_library",
        "@com_github_romnnn_testcontainers//mongo:go_default_library",
        "@com_github_romnnn_testcontainers//rabbitmq:go_default_library",
        "@com_github_romnnn_testcontainers//redis:go_default_library",
        "@com_github_romnnn_testcontainers_go//:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@org_golang_google_grpc//test/bufconn:go_default_library",
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
