load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

MODALITY_PROTOS = [
    "//models/internal/modalities/air:air_proto",
    "//models/internal/modalities/maritime:maritime_proto",
    "//models/internal/modalities/road:road_proto",
    "//models/internal/modalities/rail:rail_proto",
]

MODALITY_GO_PROTOS = [
    "//models/internal/modalities/air:air_go_proto",
    "//models/internal/modalities/maritime:maritime_go_proto",
    "//models/internal/modalities/road:road_go_proto",
    "//models/internal/modalities/rail:rail_go_proto",
]

proto_library(
    name = "event_proto",
    srcs = ["event.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "event_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/event",
    proto = ":event_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "delay_proto",
    srcs = ["delay.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "@com_google_protobuf//:duration_proto",
    ],
)

go_proto_library(
    name = "delay_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/delay",
    proto = ":delay_proto",
    visibility = ["//visibility:public"],
    deps = [],
)

java_proto_library(
    name = "delay_java_proto",
    visibility = ["//visibility:public"],
    deps = [":delay_proto"],
)

proto_library(
    name = "cargo_proto",
    srcs = ["cargo.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal:delay_proto",
        "//models/internal:station_proto",
        "//models/internal/types:ids_proto",
    ],
)

go_proto_library(
    name = "cargo_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/cargo",
    proto = ":cargo_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal:delay_go_proto",
        "//models/internal:station_go_proto",
        "//models/internal/types:ids_go_proto",
    ],
)

proto_library(
    name = "station_proto",
    srcs = ["station.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal:delay_proto",
        "//models/internal/types:coordinate_proto",
        "//models/internal/types:country_proto",
        "//models/internal/types:ids_proto",
        "//models/internal/types:transport_proto",
        "@com_google_protobuf//:timestamp_proto",
    ] + MODALITY_PROTOS,
)

go_proto_library(
    name = "station_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/station",
    proto = ":station_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/internal:delay_go_proto",
        "//models/internal/types:coordinate_go_proto",
        "//models/internal/types:country_go_proto",
        "//models/internal/types:ids_go_proto",
        "//models/internal/types:transport_go_proto",
    ] + MODALITY_GO_PROTOS,
)

proto_library(
    name = "transport_proto",
    srcs = ["transport.proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":station_proto",
        ":cargo_proto",
        ":delay_proto",
        "@com_google_protobuf//:timestamp_proto",
        "//models/internal/types:ids_proto",
        "//models/internal/types:carrier_proto",
        "//models/internal/types:transport_proto",
    ] + MODALITY_PROTOS,
)

go_proto_library(
    name = "transport_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/transport",
    proto = ":transport_proto",
    visibility = ["//visibility:public"],
    deps = [
        ":station_go_proto",
        ":cargo_go_proto",
        ":delay_go_proto",
        "//models/internal/types:ids_go_proto",
        "//models/internal/types:carrier_go_proto",
        "//models/internal/types:transport_go_proto",
    ] + MODALITY_GO_PROTOS,
)

filegroup(
    name = "internal",
    srcs = [
        ":event_go_proto",
        ":station_go_proto",
        ":transport_go_proto",
        "//models/internal/notifications",
        "//models/internal/types",
        "//models/internal/updates",
    ],
    visibility = ["//visibility:public"],
)
