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

multirun(
    name = "publish",
    commands = [
        "//osiris:publish",
        "//core:publish",
        "//auxiliary/producers/replayer:publish",
    ],
    parallel = True,
)

# Build the docker images and tags them for use in local dev env
multirun(
    name = "images",
    commands = [
        "//osiris:images",
        "//core:image",
        "//auxiliary/producers/replayer:image",
    ],
    parallel = True,
)

# Test Suites:
# Smoke tests (fast and light)
# Unit tests (superset of the smoke tests)
# Integration tests (Mostly long tests that require docker and can only be run on linux)
# Internal

test_suite(
    name = "smoke",
    tags = ["smoke"],
    tests = [
        "//core:smoke",
        "//osiris/authentication:smoke",
        "//osiris/notification:smoke",
        "//osiris/query:smoke",
        "//osiris/user_management:smoke",
    ]
)

test_suite(
    name = "unit",
    tags = ["unit"],
    tests = [
        "//core:unit",
        "//osiris/authentication:unit",
        "//osiris/notification:unit",
        "//osiris/query:unit",
        "//osiris/user_management:unit",
    ]
)

test_suite(
    name = "integration",
    tags = ["integration"],
    tests = [
        "//core:integration",
        "//osiris/authentication:integration",
        "//osiris/notification:integration",
        "//osiris/query:integration",
        "//osiris/user_management:integration",
    ]
)

test_suite(
    name = "internal",
    tags = ["internal"],
    tests = [
        "//core:internal",
        "//osiris/authentication:internal",
        "//osiris/notification:internal",
        "//osiris/query:internal",
        "//osiris/user_management:internal",
    ]
)


