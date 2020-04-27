load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("//:rules.bzl", "gql_proto_compile")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
    name = "gql_proto",
    srcs = ["gql.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:descriptor_proto"],
)

proto_library(
    name = "query_proto",
    srcs = ["query.proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":gql_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

go_proto_library(
    name = "gql_go_proto",
    importpath = "github.com/bptlab/cepta/models/gql/gql",
    proto = ":gql_proto",
    visibility = ["//visibility:public"],
    deps = [
        # "@com_google_protobuf//ptypes/descriptor:go_default_library",
    ],
)

go_proto_library(
    name = "query_go_proto",
    importpath = "github.com/bptlab/cepta/models/gql/query",
    proto = ":query_proto",
    visibility = ["//visibility:public"],
    deps = [
        ":gql_go_proto"
    ],
)

gql_proto_compile(
    name = "query_gql_proto",
    deps = [":query_proto"],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "gql",
    srcs = [
        ":gql_go_proto",
        ":query_go_proto",
        ":query_gql_proto",
    ],
    visibility = ["//visibility:public"],
)