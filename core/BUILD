java_binary(
    name = "core",
    srcs = glob(["src/main/java/org/bptlab/cepta/*.java"]),
    main_class = "org.bptlab.cepta.Main",
    resources = glob([
        "src/main/resources/**",
    ]),
    deps = [
        ":operators",
        ":consumers",
        ":converters",
        ":types",
        ":config",
        ":constants",
        "@maven//:info_picocli_picocli",
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
    name = "operators",
    srcs = glob(["src/main/java/org/bptlab/cepta/operators/*.java"]),
)

java_library(
    name = "consumers",
    srcs = glob(["src/main/java/org/bptlab/cepta/consumers/*.java"]),
    deps = [
        "@maven//:org_apache_kafka_kafka_clients",
    ],
)

java_library(
    name = "converters",
    srcs = glob(["src/main/java/org/bptlab/cepta/utils/converters/*.java"]),
)

java_library(
    name = "types",
    srcs = glob(["src/main/java/org/bptlab/cepta/utils/types/*.java"]),
)

java_library(
    name = "config",
    srcs = glob(["src/main/java/org/bptlab/cepta/config/*.java"]),
)

java_library(
    name = "constants",
    srcs = glob(["src/main/java/org/bptlab/cepta/config/constants/*.java"]),
)
