load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "coordinate_proto",
    srcs = ["coordinate.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "coordinate_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/types/coordinate",
    proto = ":coordinate_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "coordinate_java_proto",
    visibility = ["//visibility:public"],
    deps = [":coordinate_proto"],
)

proto_library(
    name = "ids_proto",
    srcs = ["ids.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "ids_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/types/ids",
    proto = ":ids_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "ids_java_proto",
    visibility = ["//visibility:public"],
    deps = [":ids_proto"],
)

proto_library(
    name = "carrier_proto",
    srcs = ["carrier.proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":ids_proto",
    ],
)

go_proto_library(
    name = "carrier_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/types/carrier",
    proto = ":carrier_proto",
    visibility = ["//visibility:public"],
    deps = [
        ":ids_go_proto",
    ],
)

java_proto_library(
    name = "carrier_java_proto",
    visibility = ["//visibility:public"],
    deps = [":carrier_proto"],
)

proto_library(
    name = "result_proto",
    srcs = ["result.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "result_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/types/result",
    proto = ":result_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "result_java_proto",
    visibility = ["//visibility:public"],
    deps = [":result_proto"],
)

proto_library(
    name = "country_proto",
    srcs = ["country.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "country_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/types/result",
    proto = ":country_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "country_java_proto",
    visibility = ["//visibility:public"],
    deps = [":country_proto"],
)

proto_library(
    name = "transport_proto",
    srcs = ["transport.proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":ids_proto"
    ],
)

go_proto_library(
    name = "transport_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/types/transport",
    proto = ":transport_proto",
    visibility = ["//visibility:public"],
    deps = [":ids_go_proto"],
)

java_proto_library(
    name = "transport_java_proto",
    visibility = ["//visibility:public"],
    deps = [":transport_proto"],
)

proto_library(
    name = "users_proto",
    srcs = ["users.proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":ids_proto",
        ":transport_proto",
    ],
)

go_proto_library(
    name = "users_go_proto",
    importpath = "github.com/bptlab/cepta/models/internal/types/users",
    proto = ":users_proto",
    visibility = ["//visibility:public"],
    deps = [
        ":ids_go_proto",
        ":transport_go_proto",
    ],
)

java_proto_library(
    name = "users_java_proto",
    visibility = ["//visibility:public"],
    deps = [":users_proto"],
)

filegroup(
    name = "types",
    srcs = [
        ":carrier_go_proto",
        ":carrier_java_proto",
        ":carrier_proto",
        ":coordinate_go_proto",
        ":coordinate_java_proto",
        ":coordinate_proto",
        ":country_go_proto",
        ":country_java_proto",
        ":country_proto",
        ":ids_go_proto",
        ":ids_java_proto",
        ":ids_proto",
        ":result_go_proto",
        ":result_java_proto",
        ":result_proto",
        ":transport_go_proto",
        ":transport_java_proto",
        ":transport_proto",
        ":users_go_proto",
        ":users_java_proto",
        ":users_proto",
    ],
    visibility = ["//visibility:public"],
)
