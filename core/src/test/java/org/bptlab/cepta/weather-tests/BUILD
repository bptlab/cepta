load("@rules_java//java:defs.bzl", "java_test", "java_library")


java_test(
    name = "weather-live-train-join",
    srcs = ["WeatherLiveTrainJoinTests.java"],
    test_class = "org.bptlab.cepta.WeatherLiveTrainJoinTests",
    deps = [
        "//core/src/main/java/org/bptlab/cepta/operators",
        "//core/src/main/java/org/bptlab/cepta/config",
        "//core/src/test/java/org/bptlab/cepta/providers",
        
        "//models/events:train_delay_notification_java_proto",
        "//models/events:weather_data_java_proto",
        "//models/events:live_train_data_java_proto",

        "@maven//:org_javatuples_javatuples",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:junit_junit_4_13",
        "@maven//:org_testcontainers_testcontainers",
        "@maven//:org_testcontainers_kafka",
    ],
)


java_test(
    name = "weather-location-correlation",
    srcs = ["WeatherLocationCorrelationTests.java"],
    test_class = "org.bptlab.cepta.WeatherLocationCorrelationTests",
    deps = [
        "//core/src/main/java/org/bptlab/cepta/operators",
        "//core/src/main/java/org/bptlab/cepta/config",
        "//core/src/test/java/org/bptlab/cepta/providers",
        
        "//models/events:train_delay_notification_java_proto",
        "//models/events:weather_data_java_proto",
        "//models/events:live_train_data_java_proto",

        "@maven//:org_javatuples_javatuples",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:junit_junit_4_13",
        "@maven//:org_testcontainers_testcontainers",
        "@maven//:org_testcontainers_kafka",
        "@maven//:org_testcontainers_postgresql",
        "@maven//:org_postgresql_postgresql",
    ],
)

test_suite(
    name = "all",
    tests = [
        ":weather-live-train-join",
        ":weather-location-correlation",
    ],
)