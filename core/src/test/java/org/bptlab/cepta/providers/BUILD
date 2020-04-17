load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "providers",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//core/src/test/java/org/bptlab/cepta/containers",
        "//models/events:live_train_data_java_proto",
        "//models/events:planned_train_data_java_proto",
        "//models/events:weather_data_java_proto",
        "//models/grpc:replayer_java_grpc",
        "//models/grpc:replayer_java_proto",
        "//models/grpc:replayer_proto",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//netty",
        # "@io_grpc_grpc_java//protobuf",
        # "@io_grpc_grpc_java//stub",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_javatuples_javatuples",
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
