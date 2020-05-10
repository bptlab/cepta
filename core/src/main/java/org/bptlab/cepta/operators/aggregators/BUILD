load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//models/events:event_java_proto",
        "//models/internal:event_java_proto",
        "//models/internal:station_java_proto",
        "//models/internal:transport_java_proto",
        "//models/internal/types:ids_java_proto",
        "//models/internal/updates:live_update_java_proto",
        "//models/internal/updates:plan_update_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_apache_flink_flink_cep_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
    ],
)
