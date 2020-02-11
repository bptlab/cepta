load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")

go_library(
    name = "livetraindatareplayer",
    srcs = [
        "LiveTrainDataReplayer.go"
    ],
    importpath = "github.com/bptlab/cepta/auxiliary/producers/traindataproducer/traindataproducer_test/livetraindatareplayer",
    visibility = ["//visibility:public"],
    deps = [
        "//osiris/lib:db",
        "//models/grpc:replayer_go_proto",
        "//models/events:live_train_data_go_proto",
        "//models/constants:constants_go_proto",
        "//osiris/lib/kafka:producer",
        "//auxiliary/producers/producer:replayer",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
    ],
)