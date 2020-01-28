java_binary(
    name = "core",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "org.bptlab.cepta.Main",
    resources = glob([
        "src/main/resources/**",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        ":operators",
        ":consumers",
        ":converters",
        ":serialization",
        ":types",
        ":config",
        ":constants",
        "//models/events:planned_train_data_java_proto",
        "//models/events:live_train_data_java_proto",
        "//models/events:train_delay_notification_java_proto",
        "//models/events:predicted_train_data_java_proto",
        "//models/events:train_information_data_java_proto",
        "@maven//:commons_io_commons_io_2_6",
        "@maven//:joda_time_joda_time_2_9_7",
        "@maven//:info_picocli_picocli",
        "@maven//:com_twitter_chill_protobuf",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_avro",
        "@maven//:org_apache_avro_avro",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_kafka_kafka_clients",
        "@maven//:org_slf4j_slf4j_log4j12",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:com_github_jasync_sql_jasync_postgresql",
        "@maven//:com_github_jasync_sql_jasync_common",
    ],
)

java_library(
    name = "core-testlib",
    srcs = glob(["src/test/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":core",
        "@maven//:org_testng_testng_7_1_0",
    ],
)

java_test(
    name="core-tests",
    size="small",
    runtime_deps=[
        ":core",
        ":core-testlib",
        "@maven//:org_testng_testng_7_1_0",
    ],
    data=["src/test/testng.xml"],
    use_testrunner=False,
    main_class="org.testng.TestNG",
    args=["core/src/test/testng.xml"],
)

java_library(
    name = "operators",
    srcs = glob(["src/main/java/org/bptlab/cepta/operators/*.java"]),
    deps = [
        "@maven//:com_github_jasync_sql_jasync_postgresql",
        "@maven//:com_github_jasync_sql_jasync_common",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "//models/events:planned_train_data_java_proto",
        "//models/events:live_train_data_java_proto",
        ":converters",
        ":config",
    ],
)

java_library(
    name = "serialization",
    srcs = glob(["src/main/java/org/bptlab/cepta/serialization/*.java"]),
    deps = [
        "@maven//:commons_io_commons_io",
        "@maven//:org_apache_flink_flink_avro",
        "@maven//:org_apache_avro_avro",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_kafka_kafka_clients",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:com_google_protobuf_protobuf_java",
    ],
)

java_library(
    name = "consumers",
    srcs = glob(["src/main/java/org/bptlab/cepta/consumers/*.java"]),
    deps = [
        "@maven//:org_apache_kafka_kafka_clients",
        "@maven//:info_picocli_picocli",
        "//models/events:live_train_data_java_proto",
        ":config",
    ],
)

java_library(
    name = "converters",
    srcs = glob(["src/main/java/org/bptlab/cepta/utils/converters/*.java"]),
    deps = [
        "@maven//:com_github_jasync_sql_jasync_postgresql",
        "@maven//:com_github_jasync_sql_jasync_common",
        "@maven//:joda_time_joda_time",
        "@maven//:org_slf4j_slf4j_log4j12",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:info_picocli_picocli",
        "//models/events:train_information_data_java_proto",
        "//models/events:predicted_train_data_java_proto",
        "//models/events:planned_train_data_java_proto",
        "//models/events:live_train_data_java_proto",
    ],
)

java_library(
    name = "types",
    srcs = glob(["src/main/java/org/bptlab/cepta/utils/types/*.java"]),
    deps = [],
)

java_library(
    name = "config",
    srcs = glob(["src/main/java/org/bptlab/cepta/config/*.java"]),
    deps = [
        ":constants",
        "@maven//:info_picocli_picocli",
        "@maven//:org_apache_kafka_kafka_clients",
    ],
)

java_library(
    name = "constants",
    srcs = glob(["src/main/java/org/bptlab/cepta/config/constants/*.java"]),
    deps = [],
)
