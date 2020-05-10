load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "update_proto",
    srcs = ["Update.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "update_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/updates/update",
    proto = ":update_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "update_java_proto",
    visibility = ["//visibility:public"],
    deps = [":update_proto"],
)

proto_library(
    name = "position_update_proto",
    srcs = ["PositionUpdate.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:coordinate_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "position_update_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/updates/position",
    proto = ":position_update_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:coordinate_go_proto",
    ],
)

java_proto_library(
    name = "position_update_java_proto",
    visibility = ["//visibility:public"],
    deps = [":position_update_proto"],
)

proto_library(
    name = "plan_update_proto",
    srcs = ["PlanUpdate.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:ids_proto",
        "//models/internal/types:station_proto",
        "//models/internal/types:transport_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "plan_update_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/updates/plan",
    proto = ":plan_update_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:ids_go_proto",
        "//models/internal/types:station_go_proto",
        "//models/internal/types:transport_go_proto",
    ],
)

java_proto_library(
    name = "plan_update_java_proto",
    visibility = ["//visibility:public"],
    deps = [":plan_update_proto"],
)

proto_library(
    name = "live_update_proto",
    srcs = ["LiveUpdate.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:ids_proto",
        "//models/internal/types:station_proto",
        "//models/internal/types:transport_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "live_update_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/updates/live",
    proto = ":live_update_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:ids_go_proto",
        "//models/internal/types:station_go_proto",
        "//models/internal/types:transport_go_proto",
    ],
)

java_proto_library(
    name = "live_update_java_proto",
    visibility = ["//visibility:public"],
    deps = [":live_update_proto"],
)

proto_library(
    name = "weather_update_proto",
    srcs = ["WeatherUpdate.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:coordinate_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "weather_update_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/updates/weather",
    proto = ":weather_update_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal/types:coordinate_go_proto",
    ],
)

java_proto_library(
    name = "weather_update_java_proto",
    visibility = ["//visibility:public"],
    deps = [":weather_update_proto"],
)

filegroup(
    name = "updates",
    srcs = [
        ":plan_go_proto",
        ":plan_java_proto",
        ":position_update_go_proto",
        ":position_update_java_proto",
        ":update_go_proto",
        ":update_java_proto",
    ],
    visibility = ["//visibility:public"],
)
