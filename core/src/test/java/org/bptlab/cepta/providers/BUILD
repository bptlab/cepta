load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "providers",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
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

test_suite(name = "smoke", tags = ["-docker", "-internal"])
test_suite(name = "unit", tags = ["unit", "-internal"])
test_suite(name = "integration", tags = ["integration", "-internal"])
test_suite(name = "internal")