load("@rules_proto_grpc//:plugin.bzl", "proto_plugin")

proto_plugin(
    name = "proto_gql_plugin",
    options = [
        "paths=source_relative",
        "plugins=gql",
    ],
    # protoc_plugin_name = "gql",
    outputs = ["{protopath}.pb.graphqls"],
    tool = "@com_github_golang_protobuf//protoc-gen-go",
    exclusions = [
        "google/api",
        "google/protobuf",
    ],
    visibility = ["//visibility:public"],
)