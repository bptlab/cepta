load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "postgres.go",
        "mongo.go",
        "tmpmodels.go",  # TODO: Drop, currently only used by the query microservice?
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/db",
    visibility = ["//visibility:public"],
    deps = [
        "//osiris/lib:cli",
        "//models/events:live_train_data_go_proto",
        "//models/events:weather_data_go_proto",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo/options:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo/readpref:go_default_library",
    ],
)