load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "delay-shift",
    size = "medium",
    srcs = ["DelayShiftFunctionTests.java"],
    tags = [
        "docker",
        "integration",
    ],
    test_class = "org.bptlab.cepta.DelayShiftFunctionTests",
    deps = [
        "//core/src/main/java/org/bptlab/cepta/config:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/operators:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/functions:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/notification:java_default_library",
        "//core/src/test/java/org/bptlab/cepta/containers:java_default_library",
        "//core/src/test/java/org/bptlab/cepta/providers:java_default_library",
        "//models/events:live_train_data_java_proto",
        "//models/events:planned_train_data_java_proto",
        "//models/internal:delay_java_proto",
        "//models/internal/notifications:notification_java_proto",
        "//models/internal/types:ids_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:junit_junit_4_13",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_javatuples_javatuples",
        "@maven//:org_postgresql_postgresql",
        "@maven//:org_testcontainers_kafka",
        "@maven//:org_testcontainers_postgresql",
        "@maven//:org_testcontainers_testcontainers",
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
