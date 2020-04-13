load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "replayer_proto",
    srcs = ["replayer.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

filegroup(
    name = "models",
    srcs = [
        "//models/constants",
        "//models/events",
        "//models/internal",
        "//models/gql",
        "//models/grpc",
    ],
    visibility = ["//visibility:public"],
)

