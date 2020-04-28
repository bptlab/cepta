load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "extractor.go",
        "mongo.go",
        "postgres.go",
    ],
    importpath = "github.com/bptlab/cepta/auxiliary/producers/replayer/extractors",
    visibility = ["//visibility:public"],
    deps = [
        "//models/events:event_go_proto",
        "//models/grpc:replayer_go_grpc",
        "//osiris/lib/utils:go_default_library",
        "//osiris/lib/db:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@com_github_romnnn_bsonpb//:go_default_library",
        "@org_mongodb_go_mongo_driver//bson:go_default_library",
        "@org_mongodb_go_mongo_driver//bson/primitive:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo/options:go_default_library",
    ],
)
