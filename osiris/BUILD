load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun", "command")

filegroup(
    name = "osiris",
    srcs = [
        "//osiris/notification",
        "//osiris/auth",
        "//osiris/usermgmt",
        "//osiris/lib",
    ],
    visibility = ["//visibility:public"],
)

multirun(
    name = "build-images",
    commands = [
        "//osiris/notification:build-image",
        "//osiris/auth:build-image",
        "//osiris/usermgmt:build-image",
    ],
    parallel = True,
    visibility = ["//visibility:public"],
)

command(
    name = "run_auth",
    command = "//osiris/auth",
    arguments = [
        "--port",
        "-8080",
    ],
)
command(
    name = "run_usermgmt",
    command = "//osiris/usermgmt",
    arguments = [
        "--port",
        "-8079",
    ],
)

multirun(
    name = "run_all",
    commands = [
        ":run_auth",
        ":run_usermgmt",
    ],
    parallel = True,
)

multirun(
    name = "publish",
    commands = [
        "//osiris/notification:publish",
        "//osiris/auth:publish",
        "//osiris/usermgmt:publish",
    ],
    parallel = True,
    visibility = ["//visibility:public"],
)

