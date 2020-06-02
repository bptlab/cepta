load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core/src/main/java/org/bptlab/cepta/config:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/database:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/database/mongohelper:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/functions:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/notification:java_default_library",
        "//core/src/test/java/org/bptlab/cepta/containers:java_default_library",
        "//models/constants:topic_java_proto",
        "//models/events:event_java_proto",
        "//models/events:live_train_data_java_proto",
        "//models/events:location_data_java_proto",
        "//models/events:planned_train_data_java_proto",
        "//models/events:weather_data_java_proto",
        "//models/grpc:replayer_java_grpc",
        "//models/grpc:replayer_java_proto",
        "//models/grpc:replayer_proto",
        "//models/internal:delay_java_proto",
        "//models/internal/notifications:notification_java_proto",
        "//models/internal/types:ids_java_proto",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//netty",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_google_protobuf_protobuf_java_util",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_javatuples_javatuples",
        "@maven//:org_mongodb_bson",
        "@maven//:org_mongodb_mongodb_driver_reactivestreams",
        "@maven//:org_mongodb_mongodb_driver_sync",
        "@maven//:org_testcontainers_testcontainers",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_slf4j_slf4j_log4j12",
    ],
)

test_suite(
    name = "smoke",
    tags = [
        "-docker",
        "-internal",
    ],
)

test_suite(
    name = "unit",
    tags = [
        "-internal",
        "unit",
    ],
)

test_suite(
    name = "integration",
    tags = [
        "-internal",
        "integration",
    ],
)

test_suite(name = "internal")
