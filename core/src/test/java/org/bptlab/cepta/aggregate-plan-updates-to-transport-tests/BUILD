load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "aggregate-plan-updates-to-transport-tests",
    size = "small",
    srcs = ["AggregatePlanUpdatesToTransportTest.java"],
    tags = ["unit"],
    test_class = "org.bptlab.cepta.AggregatePlanUpdatesToTransportTest",
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
        # test
        "@com_google_protobuf//:protobuf_java",
        "//models/internal/modalities/maritime:maritime_java_proto",
        "//models/internal/modalities/rail:rail_java_proto",
        "//models/internal/modalities/road:road_java_proto",
        "//models/internal/modalities/air:air_java_proto",
        "//models/internal/types:carrier_java_proto",
        "//models/internal/types:coordinate_java_proto",
        "//models/internal/types:country_java_proto",
        "//models/internal/types:station_java_proto",
        "//models/internal/types:transport_java_proto",
        "//models/internal:cargo_java_proto",
        "@maven//:de_javakaffee_kryo_serializers",
        "@maven//:com_esotericsoftware_kryo_kryo",
        # "@testing//:com_twitter_chill_protobuf",
        "@maven//:junit_junit_4_13",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_javatuples_javatuples",
        "@testing//:org_apache_flink_flink_runtime_2_11_tests",
        "@testing//:org_apache_flink_flink_streaming_java_2_11",
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
