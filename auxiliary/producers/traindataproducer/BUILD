load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")

go_binary(
    name = "traindataproducer",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_image(
    name = "image",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

container_push(
  name = "publish",
  format = "Docker",
  image = ":image",
  registry = "index.docker.io",
  repository = "ceptaorg/traindataproducer",
  tag = "master",
  visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go"
    ],
    importpath = "github.com/bptlab/cepta/aux/producers/traindataproducer",
    visibility = ["//visibility:private"],
    deps = [
        ":livetraindatareplayer",
        "//models/grpc:replayer_go_proto",
        "//osiris/lib:db",
        "//osiris/lib:cli",
        "//osiris/lib:kafkaproducer",
        "//osiris/lib:utils",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)

go_library(
    name = "livetraindatareplayer",
    srcs = [
        "LiveTrainDataReplayer.go"
    ],
    importpath = "github.com/bptlab/cepta/aux/producers/traindataproducer/livetraindatareplayer",
    visibility = ["//visibility:private"],
    deps = [
        "//osiris/lib:db",
        "//models/grpc:replayer_go_proto",
        "//models/events:live_train_data_go_proto",
        "//models/constants:constants_go_proto",
        "//osiris/lib:kafkaproducer",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
    ],
)
