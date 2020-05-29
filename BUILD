load("@bazel_gazelle//:def.bzl", "gazelle")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun")
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
    name = "build-images",
    commands = [
        "//osiris:build-images",
        "//core:build-image",
        "//auxiliary/producers/replayer:build-image",
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
        "//auxiliary/producers/replayer:smoke",
        "//osiris/lib:smoke",
        "//osiris/auth:smoke",
        "//osiris/notification:smoke",
        "//osiris/usermgmt:smoke",
    ]
)

test_suite(
    name = "unit",
    tags = ["unit"],
    tests = [
        "//core:unit",
        "//auxiliary/producers/replayer:unit",
        "//osiris/lib:unit",
        "//osiris/auth:unit",
        "//osiris/notification:unit",
        "//osiris/usermgmt:unit",
    ]
)

test_suite(
    name = "integration",
    tags = ["integration"],
    tests = [
        "//core:integration",
        "//auxiliary/producers/replayer:integration",
        "//osiris/lib:integration",
        "//osiris/auth:integration",
        "//osiris/notification:integration",
        "//osiris/usermgmt:integration",
    ]
)

test_suite(
    name = "internal",
    tags = ["internal"],
    tests = [
        "//core:internal",
        "//auxiliary/producers/replayer:internal",
        "//osiris/lib:internal",
        "//osiris/auth:internal",
        "//osiris/notification:internal",
        "//osiris/usermgmt:internal",
    ]
)


