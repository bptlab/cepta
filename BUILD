load("@bazel_gazelle//:def.bzl", "gazelle")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun", "command")
load("@rules_proto_grpc//:plugin.bzl", "proto_plugin")

# gazelle:prefix github.com/bptlab/cepta
gazelle(name = "gazelle")

proto_plugin(
    name = "proto_gql_plugin",
    options = [
        "paths=source_relative",
        "plugins=gql",
    ],
    protoc_plugin_name = "gql",
    outputs = ["{protopath}.pb.graphqls"],
    tool = "@go_proto_gql//protoc-gen-gql",
    exclusions = [
        "google/api",
        "google/protobuf",
    ],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "cepta",
    srcs = [
        "//osiris",
        "//core",
        "//auxiliary",
    ],
)

command(
    name = "command1",
    command = "//some/label",
    arguments = [
        "-arg1",
        "value1",
        "-arg2",
    ],
    environment = {
        "ABC": "DEF",
    },
    raw_environment = {
        "PATH": "$(pwd)/path",
    },
)

command(
    name = "producer",
    command = "//auxiliary/producers/traindataproducer",
    environment = {
        "PORT": "8080",
    },
    raw_environment = {
       "PATH": "$(pwd)/path",
    },
)

multirun(
    name = "run_all",
    commands = [
        ":producer",
        # "//core",
    ],
    parallel = True,
)

test_suite(
    name = "all",
    tests = [
        "//core/src/test/java/org/bptlab/cepta:all",
    ],
)
