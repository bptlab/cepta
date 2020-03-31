load("@rules_java//java:defs.bzl", "java_test", "java_library")

java_test(
    name = "data-cleansing",
    srcs = ["DataCleansingTests.java"],
    test_class = "org.bptlab.cepta.DataCleansingTests",
    deps = [
        "//core/src/main/java/org/bptlab/cepta/operators",
        "//core/src/main/java/org/bptlab/cepta/config",
        "//core/src/test/java/org/bptlab/cepta/providers",

        "//models/events:live_train_data_java_proto",

        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:junit_junit_4_13",
    ],
)

test_suite(
    name = "all",
    tests = [
        ":data-cleansing",
    ],
)
