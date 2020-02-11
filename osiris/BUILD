load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun")

filegroup(
    name = "osiris",
    srcs = [
        "//osiris/query",
        "//osiris/notification",
        "//osiris/authentication",
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
    ],
    parallel = True,
    visibility = ["//visibility:public"],
)

