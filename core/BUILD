load("@rules_java//java:defs.bzl", "java_binary")
load("@io_bazel_rules_docker//java:image.bzl", "java_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "multirun", "command")

java_binary(
    name = "core",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "org.bptlab.cepta.Main",
    resources = glob([
        "src/main/resources/**",
    ]),
    stamp = True,
    visibility = ["//visibility:public"],
    deps = [
        "//core/src/main/java/org/bptlab/cepta/operators:operators",
        "//core/src/main/java/org/bptlab/cepta/consumers:consumers",
        "//core/src/main/java/org/bptlab/cepta/config:config",
        "//core/src/main/java/org/bptlab/cepta/serialization:serialization",
        "//core/src/main/java/org/bptlab/cepta/utils/types:types",
        "//models/events:planned_train_data_java_proto",
        "//models/events:live_train_data_java_proto",
        "//models/events:train_delay_notification_java_proto",
        "//models/events:predicted_train_data_java_proto",
        "//models/events:train_information_data_java_proto",
        "//models/events:weather_data_java_proto",
        "//models/constants:constants_java_proto",
        "@maven//:commons_io_commons_io_2_6",
        "@maven//:joda_time_joda_time_2_9_7",
        "@maven//:info_picocli_picocli",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_kafka_kafka_clients",
        "@maven//:org_slf4j_slf4j_log4j12",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:com_github_jasync_sql_jasync_postgresql",
        "@maven//:com_github_jasync_sql_jasync_common",
        "@maven//:org_apache_flink_flink_cep_2_11",
    ],
)

java_image(
    name = "image",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "org.bptlab.cepta.Main",
    resources = glob([
        "src/main/resources/**",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//core/src/main/java/org/bptlab/cepta/operators:operators",
        "//core/src/main/java/org/bptlab/cepta/consumers:consumers",
        "//core/src/main/java/org/bptlab/cepta/config:config",
        "//core/src/main/java/org/bptlab/cepta/serialization:serialization",
        "//core/src/main/java/org/bptlab/cepta/utils/types:types",
        "//models/events:planned_train_data_java_proto",
        "//models/events:live_train_data_java_proto",
        "//models/events:train_delay_notification_java_proto",
        "//models/events:predicted_train_data_java_proto",
        "//models/events:train_information_data_java_proto",
        "//models/events:weather_data_java_proto",
        "//models/constants:constants_java_proto",
        "@maven//:commons_io_commons_io_2_6",
        "@maven//:joda_time_joda_time_2_9_7",
        "@maven//:info_picocli_picocli",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_apache_flink_flink_core",
        "@maven//:org_apache_flink_flink_java",
        "@maven//:org_apache_flink_flink_streaming_java_2_11",
        "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
        "@maven//:org_apache_kafka_kafka_clients",
        "@maven//:org_slf4j_slf4j_log4j12",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:com_github_jasync_sql_jasync_postgresql",
        "@maven//:com_github_jasync_sql_jasync_common",
    ],
)

command(
    name = "build-image-cmd",
    command = ":image",
    arguments = [
        "--norun"
    ]
)

multirun(
    visibility = ["//visibility:public"],
    name = "build-image",
    commands = [
        ":build-image-cmd",
    ],
)

container_push(
    name = "publish",
    format = "Docker",
    image = ":image",
    registry = "index.docker.io",
    repository = "ceptaorg/core",
    tag = "{STABLE_DOCKER_TAG}",
    visibility = ["//visibility:public"],
)

sub_tests = [
    "//core/src/test/java/org/bptlab/cepta/weather-tests:weather-location-correlation",
    "//core/src/test/java/org/bptlab/cepta/weather-tests:weather-live-train-join",
    "//core/src/test/java/org/bptlab/cepta/live-planned-trains:live-planned-train-correlation",
    "//core/src/test/java/org/bptlab/cepta/data-cleansing-tests:data-cleansing",
    "//core/src/test/java/org/bptlab/cepta/data-to-database:data-to-database",
<<<<<<< HEAD
    "//core/src/test/java/org/bptlab/cepta/patterns:pattern-tests",
=======
    "//core/src/test/java/org/bptlab/cepta/remove-duplicates:remove-duplicates",
>>>>>>> dev
]

test_suite(name = "smoke", tags = ["-docker", "-internal"], tests=sub_tests)
test_suite(name = "unit", tags = ["unit", "-internal"], tests=sub_tests)
test_suite(name = "integration", tags = ["integration", "-internal"], tests=sub_tests)
test_suite(name = "internal", tests=sub_tests)