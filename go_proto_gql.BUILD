load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library")

go_binary(
    name = "protoc-gen-gql",
    embed = [":go_default_library"],
    visibility = ["//visibility:public"],
)

go_library(
    name = "go_default_library",
    srcs = glob([
        # "pb/*.go",
        # "plugin/*.go",
        # "protoc-gen-gql/*.go",
        "protoc-gen-gql/main.go",
    ]),
    importpath = "github.com/danielvladco/go-proto-gql",
    visibility = ["//visibility:private"],
    deps = [
        "@com_github_danielvladco_go_proto_gql//plugin:go_default_library",
        "@com_github_gogo_protobuf//proto:go_default_library",
        "@com_github_gogo_protobuf//protoc-gen-gogo/generator:go_default_library",
    ],
)

# "github.com/gogo/protobuf/proto"
# "github.com/gogo/protobuf/protoc-gen-gogo/generator"