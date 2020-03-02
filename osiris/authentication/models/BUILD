load("@io_bazel_rules_go//go:def.bzl", "go_library", "go_test")

go_library(
    name = "go_default_library",
    srcs = [
        "accounts.go",
        "base.go",
    ],
    importpath = "github.com/bptlab/cepta/osiris/authentication/models",
    visibility = ["//visibility:public"],
    deps = [
        "//osiris/authentication/utils:go_default_library",
        "@com_github_joho_godotenv//:go_default_library",
        "@com_github_jinzhu_gorm//:go_default_library",
        "@com_github_jinzhu_gorm//dialects/postgres:go_default_library",
        "@org_golang_x_crypto//bcrypt:go_default_library",
        "@com_github_dgrijalva_jwt_go//:go_default_library",
        "@com_github_selvatico_go_mocket//:go_default_library"
    ],
)

go_test(
    name = "go_default_test",
    srcs = ["accounts_test.go"],
    embed = [":go_default_library"],
    size = "medium"
)
