load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_github_jasync_sql_jasync_postgresql",
        "@maven//:com_github_jasync_sql_jasync_common",
        "@maven//:joda_time_joda_time",
        "@maven//:org_slf4j_slf4j_log4j12",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:info_picocli_picocli",
        "//models/events:train_information_data_java_proto",
        "//models/events:predicted_train_data_java_proto",
        "//models/events:planned_train_data_java_proto",
        "//models/events:live_train_data_java_proto",
        "//models/events:weather_data_java_proto",
    ],
)