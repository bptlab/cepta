load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "go_default_library",
    srcs = [
        "auth.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/auth/lib",
    visibility = ["//osiris/auth:__pkg__"],
    x_defs = {
        "Version": "{STABLE_VERSION}",
        "BuildTime": "{buildDate}",
    },
    deps = [
        "//ci/versioning:go_default_library",
        "//models/grpc:auth_go_proto",
        "//models/internal/types:users_go_proto",
        "//osiris/lib:cli",
        "//osiris/lib/db:go_default_library",
        "@com_github_dgrijalva_jwt_go//:go_default_library",
        "@com_github_golang_protobuf//proto:go_default_library",
        "@com_github_google_uuid//:go_default_library",
        "@com_github_gorilla_mux//:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_lib_pq//:go_default_library",
        "@com_github_romnnn_bsonpb//:go_default_library",
        "@com_github_romnnn_flatbson//:go_default_library",
        "@com_github_selvatico_go_mocket//:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
        "@org_golang_google_grpc//:go_default_library",
        "@org_golang_google_grpc//test/bufconn:go_default_library",
        "@org_mongodb_go_mongo_driver//bson:go_default_library",
        "@org_mongodb_go_mongo_driver//bson/primitive:go_default_library",
        "@org_mongodb_go_mongo_driver//mongo:go_default_library",
    ],
)
