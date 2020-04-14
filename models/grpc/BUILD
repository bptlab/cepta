load("@io_bazel_rules_go//go:def.bzl", "go_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto_grpc//github.com/grpc/grpc-web:defs.bzl", "closure_grpc_compile", "commonjs_dts_grpc_compile", "ts_grpc_compile")
load("@rules_proto_grpc//nodejs:defs.bzl", "nodejs_proto_compile")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_typescript_proto//:index.bzl", "typescript_proto_library")

# User management
proto_library(
    name = "usermgmt_proto",
    srcs = ["usermgmt.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

go_proto_library(
    name = "usermgmt_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/usermgmt",
    proto = ":usermgmt_proto",
    visibility = ["//visibility:public"],
)

# Authentication

proto_library(
    name = "authentication_proto",
    srcs = ["authentication.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

go_proto_library(
    name = "authentication_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/authentication",
    proto = ":authentication_proto",
    visibility = ["//visibility:public"],
)

# Replayer

proto_library(
    name = "replayer_proto",
    srcs = ["replayer.proto"],
    visibility = ["//visibility:public"],
    deps = [
        "//models/constants:constants_proto",
        "//models/events:checkpoint_data_proto",
        "//models/events:country_data_proto",
        "//models/events:crew_activity_data_proto",
        "//models/events:crew_prep_end_data_proto",
        "//models/events:crew_shift_data_proto",
        "//models/events:crew_transition_data_proto",
        "//models/events:delay_explanation_data_proto",
        "//models/events:gps_trip_update_proto",
        "//models/events:infrastructure_manager_data_proto",
        "//models/events:live_train_data_proto",
        "//models/events:location_data_proto",
        "//models/events:planned_train_data_proto",
        "//models/events:predicted_train_data_proto",
        "//models/events:railway_undertaking_data_proto",
        "//models/events:station_data_proto",
        "//models/events:train_delay_notification_proto",
        "//models/events:train_information_data_proto",
        "//models/events:vehicle_data_proto",
        "//models/events:weather_data_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "replayer_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/replayer",
    proto = ":replayer_proto",
    visibility = ["//visibility:public"],
    deps = [
        "//models/constants:constants_go_proto",
        "//models/events:checkpoint_data_go_proto",
        "//models/events:country_data_go_proto",
        "//models/events:crew_activity_data_go_proto",
        "//models/events:crew_prep_end_data_go_proto",
        "//models/events:crew_shift_data_go_proto",
        "//models/events:crew_transition_data_go_proto",
        "//models/events:delay_explanation_data_go_proto",
        "//models/events:gps_trip_update_go_proto",
        "//models/events:infrastructure_manager_data_go_proto",
        "//models/events:live_train_data_go_proto",
        "//models/events:location_data_go_proto",
        "//models/events:planned_train_data_go_proto",
        "//models/events:predicted_train_data_go_proto",
        "//models/events:railway_undertaking_data_go_proto",
        "//models/events:station_data_go_proto",
        "//models/events:train_delay_notification_go_proto",
        "//models/events:train_information_data_go_proto",
        "//models/events:vehicle_data_go_proto",
        "//models/events:weather_data_go_proto",
    ],
)

typescript_proto_library(
    name = "replayer_ts_proto",
    proto = ":replayer_proto",
)

ts_grpc_compile(
    name = "replayer_ts_grpc_proto",
    deps = [":replayer_proto"],
)

commonjs_dts_grpc_compile(
    name = "replayer_commonjs_dts_grpc_proto",
    deps = ["replayer_proto"],
)

closure_grpc_compile(
    name = "replayer_closure_grpc_proto",
    deps = ["replayer_proto"],
)

nodejs_proto_compile(
    name = "replayer_nodejs_proto",
    deps = ["replayer_proto"],
)

go_library(
    name = "go_default_library",
    embed = [":replayer_go_proto"],
    importpath = "github.com/bptlab/cepta/models/grpc/replayer",
    visibility = ["//visibility:public"],
)

filegroup(
    name = "grpc",
    srcs = [
        # ":replayer_ts_grpc_proto",
        # ":replayer_commonjs_dts_grpc_proto",
        # "replayer_nodejs_proto",
        # ":replayer_ts_proto",
        ":replayer_go_proto",
    ],
    visibility = ["//visibility:public"],
)
