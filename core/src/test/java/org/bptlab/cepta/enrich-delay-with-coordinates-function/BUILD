load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "enrich-delay-with-coordinates-function",
    size = "medium",
    srcs = ["EnrichDelayWithCoordinatesFunctionTests.java"],
    tags = [
        "docker",
        "integration",
    ],
    test_class = "org.bptlab.cepta.EnrichDelayWithCoordinatesFunctionTests",
    deps = [
        "//core/src/main/java/org/bptlab/cepta/config:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/operators:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/database:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/database/mongohelper:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/functions:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/notification:java_default_library",
        "//core/src/test/java/org/bptlab/cepta/providers:java_default_library",
        "//models/events:live_train_data_java_proto",
        "//models/events:location_data_java_proto",
        "//models/events:planned_train_data_java_proto",
        "//models/internal/notifications:notification_java_proto",
        "//models/internal/types:coordinate_java_proto",
        "//models/internal/types:coordinate_proto",
        "//models/internal/types:ids_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:junit_junit_4_13",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_runtime_2_11",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_javatuples_javatuples",
        "@maven//:org_mongodb_bson",
        "@maven//:org_mongodb_mongodb_driver_core",
        "@maven//:org_mongodb_mongodb_driver_reactivestreams",
        "@maven//:org_mongodb_mongodb_driver_sync",
        "@maven//:org_postgresql_postgresql",
        "@maven//:org_testcontainers_kafka",
        "@maven//:org_testcontainers_postgresql",
        "@maven//:org_testcontainers_testcontainers",
        "@testing//:org_apache_flink_flink_runtime_2_11_tests",
        "@testing//:org_apache_flink_flink_streaming_java_2_11_tests",
        "@testing//:org_apache_flink_flink_test_utils_2_11",
        "@testing//:org_apache_flink_flink_test_utils_junit",
        "@testing//:org_apache_flink_flink_tests_tests",
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
