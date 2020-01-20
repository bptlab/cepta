load("@io_bazel_rules_go//go:def.bzl", "go_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("//:rules.bzl", "gql_proto_compile")

proto_library(
    name = "live_train_data_proto",
    srcs = ["LiveTrainData.proto"],
    visibility = ["//visibility:public"],
)

proto_library(
    name = "planned_train_data_proto",
    srcs = ["PlannedTrainData.proto"],
    visibility = ["//visibility:public"],
)

proto_library(
    name = "predicted_train_data_proto",
    srcs = ["PredictedTrainData.proto"],
    visibility = ["//visibility:public"],
)

proto_library(
    name = "train_delay_notification",
    srcs = ["TrainDelayNotification.proto"],
    visibility = ["//visibility:public"],
)

proto_library(
    name = "train_information_data",
    srcs = ["TrainInformationData.proto"],
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "live_train_data_java_proto",
    deps = [":live_train_data_proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "live_train_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/livetraindataevent",
    proto = ":live_train_data_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "planned_train_data_java_proto",
    deps = [":planned_train_data_proto"],
    visibility = ["//visibility:public"],
)