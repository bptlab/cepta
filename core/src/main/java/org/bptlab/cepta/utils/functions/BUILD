load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//models/events:live_train_data_java_proto",
        "//models/events:planned_train_data_java_proto",
        "//models/events:weather_data_java_proto",
        "//models/internal:correlateable_event_java_proto",
        "//models/internal/types:coordinate_java_proto",
        "@maven//:javax_vecmath_vecmath",
        "@maven//:com_github_davidmoten_grumpy_core",
        "@maven//:com_google_protobuf_protobuf_java_util",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_apache_flink_flink_cep_2_11",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
    ],
)
