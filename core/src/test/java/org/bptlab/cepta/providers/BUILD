load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "providers",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        # "//protos:person_java_proto",
        # "@maven//:junit_junit_4_13",
        # "@maven//:org_testcontainers_testcontainers",
        # "@maven//:org_testcontainers_kafka",
        # "//models/events:train_delay_notification_java_proto",
        "//models/events:weather_data_java_proto",
        "//models/events:live_train_data_java_proto",
        "//models/events:planned_train_data_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_javatuples_javatuples",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
    ],
)