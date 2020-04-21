load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "mongo.go",
        "kafka.go",
        "zookeeper.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/testing",
    visibility = ["//visibility:public"],
    deps = [
        "//osiris/lib/kafka:producer",
        "//osiris/lib/kafka:consumer",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib:cli",
        "@org_mongodb_go_mongo_driver//bson:go_default_library",
        "@org_mongodb_go_mongo_driver//bson/primitive:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
        "@com_github_testcontainers_testcontainers_go//:go_default_library",
        "@com_github_testcontainers_testcontainers_go//wait:go_default_library",
        "@com_github_docker_go_connections//nat:go_default_library",
    ],
)
