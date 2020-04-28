load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "notification.go",
    ],
    x_defs = {
        "Version": "{STABLE_VERSION}",
        "BuildTime": "{buildDate}",
    },
    importpath = "github.com/bptlab/cepta/osiris/notification/lib",
    visibility = ["//osiris/notification:__pkg__"],
    deps = [
        "//models/grpc:notification_go_proto",
        "//models/types:users_go_proto",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib:cli",
        "//ci/versioning:go_default_library",
        "@com_github_romnnn_flatbson//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_google_uuid//:go_default_library",
        "@org_mongodb_go_mongo_driver//bson:go_default_library",
        "@org_mongodb_go_mongo_driver//bson/primitive:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
        "@com_github_romnnn_bsonpb//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)