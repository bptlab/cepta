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
        ":serialization",
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
    deps = [
        "@maven//:com_github_jasync_sql_jasync_postgresql",
        "@maven//:com_github_jasync_sql_jasync_common",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
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
    ],
)

java_library(
    name = "consumers",
    srcs = glob(["src/main/java/org/bptlab/cepta/consumers/*.java"]),
    deps = [
        "@maven//:org_apache_kafka_kafka_clients",
        "@maven//:info_picocli_picocli",
        ":config",
    ],
)

java_library(
    name = "converters",
    srcs = glob(["src/main/java/org/bptlab/cepta/utils/converters/*.java"]),
    deps = [
        
    ],
)

java_library(
    name = "types",
    srcs = glob(["src/main/java/org/bptlab/cepta/utils/types/*.java"]),
    deps = [
        
    ],
)

java_library(
    name = "config",
    srcs = glob(["src/main/java/org/bptlab/cepta/config/*.java"]),
    deps = [
        ":constants",
    ],
)

java_library(
    name = "constants",
    srcs = glob(["src/main/java/org/bptlab/cepta/config/constants/*.java"]),
    deps = [
        
    ],
)
