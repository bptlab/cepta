load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "mongo.go",
        "postgres.go",
    ],
    importpath = "github.com/bptlab/cepta/auxiliary/producers/replayer/extractors",
    visibility = ["//visibility:public"],
    deps = [
        "//models/grpc:replayer_go_proto",
        "//osiris/lib/db:go_default_library",
        # "//osiris/lib:cli",
        # "//osiris/lib/kafka:producer",
        # "//osiris/lib:utils",
        # "//models/constants:constants_go_proto",
        # "//models/events:live_train_data_go_proto",
        # "//models/events:checkpoint_data_go_proto",
        # "@com_github_sirupsen_logrus//:go_default_library",
        # "@org_golang_google_grpc//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@org_mongodb_go_mongo_driver//bson:go_default_library",
        "@org_mongodb_go_mongo_driver//bson/primitive:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
        "@com_github_romnnn_bsonpb//:go_default_library",
        # "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        # "@com_github_urfave_cli_v2//:go_default_library",
        # "@com_github_shopify_sarama//:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
    ],
)