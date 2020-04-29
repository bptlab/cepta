load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "usermgmt.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/usermgmt/lib",
    visibility = ["//osiris/usermgmt:__pkg__"],
    x_defs = {
        "Version": "{STABLE_VERSION}",
        "BuildTime": "{buildDate}",
    },
    deps = [
        "//models/grpc:auth_go_proto",
        "//models/grpc:usermgmt_go_proto",
        "//models/internal/types:ids_go_proto",
        "//models/internal/types:transport_go_proto",
        "//models/internal/types:users_go_proto",
        "//osiris/auth:go_default_library",
        "//osiris/lib:cli",
        "//osiris/lib/db:go_default_library",
        "//osiris/lib/utils:go_default_library",
        "@com_github_dgrijalva_jwt_go//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_google_uuid//:go_default_library",
        "@com_github_romnnn_bsonpb//:go_default_library",
        "@com_github_romnnn_flatbson//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@org_mongodb_go_mongo_driver//bson:go_default_library",
        "@org_mongodb_go_mongo_driver//bson/primitive:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
    ],
)
