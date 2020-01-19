load("@io_bazel_rules_go//go:def.bzl", "go_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("//:rules.bzl", "gql_proto_compile")

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
        "//models/types:basic_proto",
    ],
)

gql_proto_compile(
    name = "query_gql_proto",
    # proto = ":gql_proto",
    deps = [":query_proto"],
)
