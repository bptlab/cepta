load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library", "go_test")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "command", "multirun")

go_binary(
    name = "replayer",
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
    repository = "ceptaorg/replayer",
    tag = "{STABLE_DOCKER_TAG}",
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "replayer.go",
        "replayers.go",
        "server.go",
    ],
    importpath = "github.com/bptlab/cepta/auxiliary/producers/replayer",
    visibility = ["//visibility:public"],
    x_defs = {
        "Version": "{STABLE_VERSION}",
        "BuildTime": "{buildDate}",
    },
    deps = [
        "//auxiliary/producers/replayer/extractors:go_default_library",
        "//ci/versioning:go_default_library",
        "//models/constants:topic_go_proto",
        "//models/events:checkpoint_data_go_proto",
        "//models/events:crew_activity_data_go_proto",
        "//models/events:crew_prep_end_data_go_proto",
        "//models/events:crew_shift_data_go_proto",
        "//models/events:crew_transition_data_go_proto",
        "//models/events:delay_explanation_data_go_proto",
        "//models/events:event_go_proto",
        "//models/events:gps_trip_update_go_proto",
        "//models/events:infrastructure_manager_data_go_proto",
        "//models/events:live_train_data_go_proto",
        "//models/events:location_data_go_proto",
        "//models/events:planned_train_data_go_proto",
        "//models/events:predicted_train_data_go_proto",
        "//models/events:railway_undertaking_data_go_proto",
        "//models/events:station_data_go_proto",
        "//models/events:train_information_data_go_proto",
        "//models/events:vehicle_data_go_proto",
        "//models/events:weather_data_go_proto",
        "//models/grpc:replayer_go_grpc",
        "//models/internal/types:result_go_proto",
        "//osiris/lib:cli",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib/kafka/producer:go_default_library",
        "//osiris/lib/utils:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@com_github_golang_protobuf//ptypes/wrappers:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@com_github_romnnn_flags4urfavecli//values:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@org_golang_google_grpc//keepalive:go_default_library",
        "@org_golang_google_grpc//metadata:go_default_library",
    ],
)

TEST_DEPS = [
    "@com_github_romnnn_bsonpb//:go_default_library",
    "@com_github_romnnn_deepequal//:go_default_library",
    "@org_mongodb_go_mongo_driver//bson:go_default_library",
    "@org_mongodb_go_mongo_driver//mongo:go_default_library",
    "@org_golang_google_grpc//test/bufconn:go_default_library",
    "@org_golang_google_grpc//:go_default_library",
    "@com_github_romnnn_testcontainers_go//:go_default_library",
    "@com_github_romnnn_testcontainers//:go_default_library",
    "@com_github_romnnn_testcontainers//kafka:go_default_library",
    "@com_github_romnnn_testcontainers//mongo:go_default_library",
    "@com_github_shopify_sarama//:go_default_library",
    "//osiris/lib/kafka/consumer:go_default_library",
    "//osiris/lib/kafka:go_default_library",
]

filegroup(
    name = "test-helpers",
    srcs = [
        "tests/assertions.go",
        "tests/mockdata.go",
        "tests/provider.go",
    ],
)

go_test(
    name = "replayer_internal",
    size = "large",
    srcs = [
        "tests/internal.go",
        ":test-helpers",
    ],
    embed = [":go_default_library"],
    tags = [
        "docker",
        "integration",
        "internal",
    ],
    deps = TEST_DEPS,
)

go_test(
    name = "replayer_integration",
    size = "large",
    srcs = [
        "tests/integration.go",
        ":test-helpers",
    ],
    embed = [":go_default_library"],
    tags = [
        "docker",
        "integration",
    ],
    deps = TEST_DEPS,
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
