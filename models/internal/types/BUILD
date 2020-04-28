load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "coordinate_proto",
    srcs = ["coordinate.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "coordinate_go_proto",
    importpath = "github.com/bptlab/cepta/models/types/coordinate",
    proto = ":coordinate_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "coordinate_java_proto",
    visibility = ["//visibility:public"],
    deps = [":coordinate_proto"],
)

proto_library(
    name = "result_proto",
    srcs = ["result.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "result_go_proto",
    importpath = "github.com/bptlab/cepta/models/types/result",
    proto = ":result_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "result_java_proto",
    visibility = ["//visibility:public"],
    deps = [":result_proto"],
)

proto_library(
    name = "transports_proto",
    srcs = ["transports.proto"],
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "transports_go_proto",
    importpath = "github.com/bptlab/cepta/models/types/transports",
    proto = ":transports_proto",
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "transports_java_proto",
    visibility = ["//visibility:public"],
    deps = [":transports_proto"],
)

proto_library(
    name = "users_proto",
    srcs = ["users.proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":transports_proto",
    ],
)

go_proto_library(
    name = "users_go_proto",
    importpath = "github.com/bptlab/cepta/models/types/users",
    proto = ":users_proto",
    visibility = ["//visibility:public"],
    deps = [
        ":transports_go_proto",
    ],
)

java_proto_library(
    name = "users_java_proto",
    visibility = ["//visibility:public"],
    deps = [":users_proto"],
)
