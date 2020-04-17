load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "patterns",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_apache_flink_flink_cep_2_11",
        "//models/events:train_delay_notification_java_proto",
        "//models/events:planned_train_data_java_proto",
        "//models/events:live_train_data_java_proto",
        "//models/events:weather_data_java_proto",
        "//core/src/main/java/org/bptlab/cepta/config:config",
        "//core/src/main/java/org/bptlab/cepta/utils/converters:converters",
        
    ],
)