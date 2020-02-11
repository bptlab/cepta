load("@rules_proto//proto:defs.bzl", "proto_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")

proto_library(
    name = "constants_proto",
    srcs = ["constants.proto"],
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "constants_java_proto",
    deps = [":constants_proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "constants_go_proto",
    importpath = "github.com/bptlab/cepta/constants",
    proto = ":constants_proto",
    visibility = ["//visibility:public"],
)

filegroup(
    name = "constants",
    srcs = [
        ":constants_go_proto", ":constants_java_proto",
    ],
    visibility = ["//visibility:public"],
)












proto_library(
    name = "planned_train_data_proto",
    srcs = ["PlannedTrainData.proto"],
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "planned_train_data_java_proto",
    deps = [":planned_train_data_proto"],
    visibility = ["//visibility:public"],
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
)

java_proto_library(
    name = "predicted_train_data_java_proto",
    deps = [":predicted_train_data_proto"],
    visibility = ["//visibility:public"],
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
)

java_proto_library(
    name = "train_delay_notification_java_proto",
    deps = [":train_delay_notification_proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "train_delay_notification_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/traindelaynotificationevent",
    proto = ":train_delay_notification_proto",
    visibility = ["//visibility:public"],
)








proto_library(
    name = "train_information_data_proto",
    srcs = ["TrainInformationData.proto"],
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "train_information_data_java_proto",
    deps = [":train_information_data_proto"],
    visibility = ["//visibility:public"],
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
)

java_proto_library(
    name = "weather_data_java_proto",
    deps = [":weather_data_proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "weather_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/weatherdataevent",
    proto = ":weather_data_proto",
    visibility = ["//visibility:public"],
)