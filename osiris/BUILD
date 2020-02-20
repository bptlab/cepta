load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun")

filegroup(
    name = "osiris",
    srcs = [
        "//osiris/query",
        "//osiris/notification",
        "//osiris/authentication",
        "//osiris/user_management",
        "//osiris/lib",
    ],
    visibility = ["//visibility:public"],
)

multirun(
    name = "images",
    commands = [
        "//osiris/query:image",
        "//osiris/notification:image",
        "//osiris/authentication:image",
        "//osiris/user_management:image",
    ],
    parallel = True,
    visibility = ["//visibility:public"],
)

multirun(
    name = "publish",
    commands = [
        "//osiris/query:publish",
        "//osiris/notification:publish",
        "//osiris/authentication:publish",
        "//osiris/user_management:publish",
    ],
    parallel = True,
    visibility = ["//visibility:public"],
)

