load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun", "command")

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

command(
    name = "run_auth",
    command = "//osiris/authentication",
    arguments = [
        # "--",
        "--port",
        "-8080",
    ],
)
command(
    name = "run_user_management",
    command = "//osiris/user_management",
    arguments = [
        # "--",
        "--port",
        "-8079",
    ],
)

multirun(
    name = "run_all",
    commands = [
        # "//osiris/query:image",
        # "//osiris/notification:image",
        ":run_auth",
        ":run_user_management",
    ],
    parallel = True,
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

