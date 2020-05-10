load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "average-speed-tests",
    size = "small",
    srcs = ["AverageSpeedTest.java"],
    tags = ["unit"],
    test_class = "org.bptlab.cepta.AverageSpeedTest",
    deps = [
        "//core/src/main/java/org/bptlab/cepta/operators/aggregators:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/serialization:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/functions:java_default_library",
        "//models/events:event_java_proto",
        "//models/internal:event_java_proto",
        "//models/internal:station_java_proto",
        "//models/internal:transport_java_proto",
        "//models/internal/types:ids_java_proto",
        "//models/internal/updates:plan_update_java_proto",
        "@maven//:junit_junit_4_13",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
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
