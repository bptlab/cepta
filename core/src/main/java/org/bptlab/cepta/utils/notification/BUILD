load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "java_default_library",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//models/events:event_java_proto",
        "//models/events:live_train_data_java_proto",
        "//models/events:planned_train_data_java_proto",
        "//models/events:weather_data_java_proto",
        "//models/internal:delay_java_proto",
        "//models/internal/notifications:notification_java_proto",
        "//models/internal/types:coordinate_java_proto",
        "//models/internal/types:ids_java_proto",
        "//models/internal/types:transport_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
    ],
)
