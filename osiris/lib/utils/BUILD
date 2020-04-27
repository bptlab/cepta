load("@io_bazel_rules_go//go:def.bzl", "go_library", "go_test")

go_library(
    name = "go_default_library",
    srcs = [
        "converters.go",
        "utils.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/lib/utils",
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_imdario_mergo//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_golang_protobuf//ptypes:go_default_library_gen",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@org_golang_google_grpc//metadata:go_default_library",
        "@com_github_mitchellh_mapstructure//:go_default_library",
    ]
)

go_test(
    name = "test",
    srcs = ["utils_test.go"],
    embed = [":go_default_library"],
    size = "small",
    tags = ["unit"],
    deps = [
        "@com_github_romnnn_deepequal//:go_default_library",
        "//models/grpc:replayer_go_grpc",
    ],
)

test_suite(name = "smoke", tags = ["-docker", "-internal"])
test_suite(name = "unit", tags = ["unit", "-internal"])
test_suite(name = "integration", tags = ["integration", "-internal"])
test_suite(name = "internal")