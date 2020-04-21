load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun", "command")

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
  repository = "ceptaorg/replayer",
  tag = "{STABLE_DOCKER_TAG}",
  visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "replayer.go",
        "server.go",
    ],
    x_defs = {
        "Version": "{STABLE_VERSION}",
        "BuildTime": "{buildDate}",
    },
    importpath = "github.com/bptlab/cepta/auxiliary/producers/replayer",
    visibility = ["//visibility:public"],
    deps = [
        "//ci/versioning:go_default_library",
        "//models/grpc:replayer_go_proto",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib:cli",
        "//osiris/lib/kafka:producer",
        "//auxiliary/producers/replayer/extractors:go_default_library",
        "//osiris/lib:utils",
        "//models/constants:constants_go_proto",
        "//models/events:checkpoint_data_go_proto",
        "//models/events:crew_activity_data_go_proto",
        "//models/events:crew_prep_end_data_go_proto",
        "//models/events:crew_shift_data_go_proto",
        "//models/events:crew_transition_data_go_proto",
        "//models/events:delay_explanation_data_go_proto",
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
        "//models/events:gps_trip_update_go_proto",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@org_golang_google_grpc//metadata:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@com_github_romnnn_flags4urfavecli//values:go_default_library",
    ],
)