load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")
load("@io_bazel_rules_docker//go:image.bzl", "go_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")

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

container_push(
  name = "publish",
  format = "Docker",
  image = ":image",
  registry = "index.docker.io",
  repository = "ceptaorg/notification",
  tag = "master",
  visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = [
        "server.go"
    ],
    importpath = "github.com/bptlab/cepta/osiris/notification",
    visibility = ["//visibility:private"],
    deps = [
        "//models/constants:constants_go_proto",
        "//models/events:train_delay_notification_go_proto",
        "//osiris/notification/websocket:go_default_library",
        "//osiris/lib/kafka:consumer",
        "//osiris/lib:cli",
        "@com_github_golang_protobuf//jsonpb:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_shopify_sarama//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
    ],
)
