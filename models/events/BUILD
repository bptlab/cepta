load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
load("@rules_typescript_proto//:index.bzl", "typescript_proto_library")
load("@rules_proto_grpc//github.com/grpc/grpc-web:defs.bzl", "ts_grpc_compile")
load("@rules_proto_grpc//nodejs:defs.bzl", "nodejs_proto_compile")

proto_library(
    name = "live_train_data_proto",
    srcs = ["LiveTrainData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "live_train_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":live_train_data_proto"],
)

go_proto_library(
    name = "live_train_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/livetraindataevent",
    proto = ":live_train_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "planned_train_data_proto",
    srcs = ["PlannedTrainData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "planned_train_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":planned_train_data_proto"],
)

go_proto_library(
    name = "planned_train_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/plannedtraindataevent",
    proto = ":planned_train_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "predicted_train_data_proto",
    srcs = ["PredictedTrainData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "predicted_train_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":predicted_train_data_proto"],
)

go_proto_library(
    name = "predicted_train_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/predictedtraindataevent",
    proto = ":predicted_train_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "train_delay_notification_proto",
    srcs = ["TrainDelayNotification.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "train_delay_notification_java_proto",
    visibility = ["//visibility:public"],
    deps = [":train_delay_notification_proto"],
)

go_proto_library(
    name = "train_delay_notification_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/traindelaynotificationevent",
    proto = ":train_delay_notification_proto",
    visibility = ["//visibility:public"],
)

ts_grpc_compile(
    name = "train_delay_notification_ts_proto",
    deps = ["train_delay_notification_proto"],
)

nodejs_proto_compile(
    name = "train_delay_notification_nodejs_proto",
    deps = ["train_delay_notification_proto"],
)

proto_library(
    name = "train_information_data_proto",
    srcs = ["TrainInformationData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "train_information_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":train_information_data_proto"],
)

go_proto_library(
    name = "train_information_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/traininformationdataevent",
    proto = ":train_information_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "weather_data_proto",
    srcs = ["WeatherData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "weather_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":weather_data_proto"],
)

go_proto_library(
    name = "weather_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/weatherdataevent",
    proto = ":weather_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "vehicle_data_proto",
    srcs = ["VehicleData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "vehicle_data_java_proto",
    deps = [":vehicle_data_proto"],  
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "vehicle_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/vehicledataevent",
    proto = ":vehicle_data_proto",
    visibility = ["//visibility:public"],
)
  
  

proto_library(
    name = "checkpoint_data_proto",
    srcs = ["CheckpointData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "checkpoint_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":checkpoint_data_proto"],
)

go_proto_library(
    name = "checkpoint_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/checkpointdataevent",
    proto = ":checkpoint_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "delay_explanation_data_proto",
    srcs = ["DelayExplanationData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "delay_explanation_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":delay_explanation_data_proto"],
)

go_proto_library(
    name = "delay_explanation_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/delayexplanationdataevent",
    proto = ":delay_explanation_data_proto",
    visibility = ["//visibility:public"],
)

filegroup(
    name = "events",
    srcs = [
        ":live_train_data_go_proto",
        ":live_train_data_java_proto",
        ":planned_train_data_go_proto",
        ":planned_train_data_java_proto",
        ":predicted_train_data_go_proto",
        ":predicted_train_data_java_proto",
        ":train_delay_notification_go_proto",
        ":train_delay_notification_java_proto",
        ":train_delay_notification_ts_proto",
        ":train_delay_notification_nodejs_proto",
        ":train_information_data_go_proto",
        ":train_information_data_java_proto",
        ":weather_data_go_proto",
        ":weather_data_java_proto",
        ":vehicle_data_go_proto",
        ":vehicle_data_java_proto",
        ":checkpoint_data_go_proto",
        ":checkpoint_data_java_proto",
        ":delay_explanation_data_go_proto",
        ":delay_explanation_data_java_proto",

    ],
    visibility = ["//visibility:public"],
)
