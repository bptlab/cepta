load("@rules_java//java:defs.bzl", "java_binary")
load("@io_bazel_rules_docker//java:image.bzl", "java_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("@com_github_atlassian_bazel_tools//multirun:def.bzl", "command", "multirun")

MAIN_DEPS = [
    "//core/src/main/java/org/bptlab/cepta/config:java_default_library",
    "//core/src/main/java/org/bptlab/cepta/operators:java_default_library",
    "//core/src/main/java/org/bptlab/cepta/patterns:java_default_library",
    "//core/src/main/java/org/bptlab/cepta/serialization:java_default_library",
    "//core/src/main/java/org/bptlab/cepta/utils/types:java_default_library",
    "//core/src/main/java/org/bptlab/cepta/utils/functions:java_default_library",
    "//core/src/main/java/org/bptlab/cepta/utils/database:java_default_library",
    "//models/constants:topic_java_proto",
    "//models/events:live_train_data_java_proto",
    "//models/events:count_of_trains_at_station_event_java_proto",
    "//models/events:planned_train_data_java_proto",
    "//models/events:location_data_java_proto",
    "//models/events:no_matching_planned_train_data_event_java_proto",
    "//models/events:predicted_train_data_java_proto",
    "//models/events:train_information_data_java_proto",
    "//models/events:weather_data_java_proto",
    "//models/events:stays_in_station_event_java_proto",
    "//models/events:event_java_proto",
    "//models/internal/types:transport_java_proto",
    "//models/internal/types:ids_java_proto",
    "//models/internal/notifications:notification_java_proto",
    "@maven//:com_github_jasync_sql_jasync_common",
    "@maven//:com_github_jasync_sql_jasync_postgresql",
    "@maven//:com_google_protobuf_protobuf_java",
    "@maven//:commons_io_commons_io_2_6",
    "@maven//:info_picocli_picocli",
    "@maven//:joda_time_joda_time_2_9_7",
    "@maven//:org_apache_commons_commons_lang3",
    "@maven//:org_apache_flink_flink_connector_kafka_0_11_2_11",
    "@maven//:org_apache_flink_flink_core",
    "@maven//:org_apache_flink_flink_java",
    "@maven//:org_apache_flink_flink_streaming_java_2_11",
    "@maven//:org_apache_kafka_kafka_clients",
    "@maven//:org_slf4j_slf4j_api",
    "@maven//:org_slf4j_slf4j_log4j12",
    "@maven//:org_apache_flink_flink_cep_2_11",
]

java_binary(
    name = "core",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "org.bptlab.cepta.Main",
    resources = glob([
        "src/main/resources/**",
    ]),
    stamp = True,
    visibility = ["//visibility:public"],
    deps = MAIN_DEPS,
)

java_image(
    name = "image",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "org.bptlab.cepta.Main",
    resources = glob([
        "src/main/resources/**",
    ]),
    visibility = ["//visibility:public"],
    deps = MAIN_DEPS,
)

command(
    name = "build-image-cmd",
    arguments = [
        "--norun",
    ],
    command = ":image",
)

multirun(
    name = "build-image",
    commands = [
        ":build-image-cmd",
    ],
    visibility = ["//visibility:public"],
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
    "//core/src/test/java/org/bptlab/cepta/count-trains-in-station-tests:count-trains-in-station",
    "//core/src/test/java/org/bptlab/cepta/weather-tests:weather-location-correlation",
    "//core/src/test/java/org/bptlab/cepta/weather-tests:weather-location-correlation-mongo",
    "//core/src/test/java/org/bptlab/cepta/weather-tests:weather-live-train-join",
    "//core/src/test/java/org/bptlab/cepta/live-planned-trains:live-planned-train-correlation",
    "//core/src/test/java/org/bptlab/cepta/live-planned-trains-mongo:live-planned-train-correlation-mongo",
    "//core/src/test/java/org/bptlab/cepta/data-cleansing-tests:data-cleansing",
    "//core/src/test/java/org/bptlab/cepta/data-to-postgres-database:data-to-postgres-database",
    "//core/src/test/java/org/bptlab/cepta/data-to-mongo-db:data-to-mongo-db",
    "//core/src/test/java/org/bptlab/cepta/remove-duplicates:remove-duplicates",
    "//core/src/test/java/org/bptlab/cepta/delay-shift:delay-shift",
    "//core/src/test/java/org/bptlab/cepta/delay-shift-mongo:delay-shift-mongo",
    "//core/src/test/java/org/bptlab/cepta/sum-of-delay-at-station:sum-of-delay-at-station",
    "//core/src/test/java/org/bptlab/cepta/patterns:stays-in-station-tests",
    "//core/src/test/java/org/bptlab/cepta/patterns:no-matching-planned-train-data-pattern-tests",
    "//core/src/test/java/org/bptlab/cepta/detect-station-delay-tests:detect-station-arrival-delay",
    "//core/src/test/java/org/bptlab/cepta/enrich-delay-with-coordinates-function:enrich-delay-with-coordinates-function"
]

test_suite(
    name = "smoke",
    tags = [
        "-docker",
        "-internal",
    ],
    tests = sub_tests,
)

test_suite(
    name = "unit",
    tags = [
        "-internal",
        "unit",
    ],
    tests = sub_tests,
)

test_suite(
    name = "integration",
    tags = [
        "-internal",
        "integration",
    ],
    tests = sub_tests,
)

test_suite(
    name = "internal",
    tests = sub_tests,
)
