load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "remove-duplicates",
    srcs = ["RemoveDuplicatesTests.java"],
    test_class = "org.bptlab.cepta.RemoveDuplicatesTests",
    size = "small",
    tags = ["unit"],
    deps = [
        "//core/src/main/java/org/bptlab/cepta/utils/functions:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/operators:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/config:java_default_library",
        "//core/src/test/java/org/bptlab/cepta/providers:java_default_library",
        
        "//models/events:live_train_data_java_proto",

        "@maven//:org_apache_flink_flink_cep_2_11",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:junit_junit_4_13",
    ],
)

test_suite(name = "smoke", tags = ["-docker", "-internal"])
test_suite(name = "unit", tags = ["unit", "-internal"])
test_suite(name = "integration", tags = ["integration", "-internal"])
test_suite(name = "internal")