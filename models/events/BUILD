load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
load("@rules_proto_grpc//github.com/grpc/grpc-web:defs.bzl", "ts_grpc_compile")
load("@rules_proto_grpc//nodejs:defs.bzl", "nodejs_proto_compile")

proto_library(
    name = "live_train_data_proto",
    srcs = ["LiveTrainData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "live_train_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":live_train_data_proto"],
)

go_proto_library(
    name = "live_train_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/livetraindataevent",
    proto = ":live_train_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "gps_trip_update_proto",
    srcs = ["GPSTripUpdate.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "gps_trip_update_java_proto",
    visibility = ["//visibility:public"],
    deps = [":gps_trip_update_proto"],
)

go_proto_library(
    name = "gps_trip_update_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/gpstripupdate",
    proto = ":gps_trip_update_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "location_data_proto",
    srcs = ["LocationData.proto"],
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "location_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":location_data_proto"],
)

go_proto_library(
    name = "location_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/locationdataevent",
    proto = ":location_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "planned_train_data_proto",
    srcs = ["PlannedTrainData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "planned_train_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":planned_train_data_proto"],
)

go_proto_library(
    name = "planned_train_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/plannedtraindataevent",
    proto = ":planned_train_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "predicted_train_data_proto",
    srcs = ["PredictedTrainData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "predicted_train_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":predicted_train_data_proto"],
)

go_proto_library(
    name = "predicted_train_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/predictedtraindataevent",
    proto = ":predicted_train_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "train_information_data_proto",
    srcs = ["TrainInformationData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "train_information_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":train_information_data_proto"],
)

go_proto_library(
    name = "train_information_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/traininformationdataevent",
    proto = ":train_information_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "weather_data_proto",
    srcs = ["WeatherData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "weather_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":weather_data_proto"],
)

go_proto_library(
    name = "weather_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/weatherdataevent",
    proto = ":weather_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "vehicle_data_proto",
    srcs = ["VehicleData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "vehicle_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":vehicle_data_proto"],
)

go_proto_library(
    name = "vehicle_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/vehicledataevent",
    proto = ":vehicle_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "checkpoint_data_proto",
    srcs = ["CheckpointData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "checkpoint_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":checkpoint_data_proto"],
)

go_proto_library(
    name = "checkpoint_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/checkpointdataevent",
    proto = ":checkpoint_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "station_data_proto",
    srcs = ["StationData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "station_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":station_data_proto"],
)

go_proto_library(
    name = "station_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/stationdataevent",
    proto = ":station_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "delay_explanation_data_proto",
    srcs = ["DelayExplanationData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "delay_explanation_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":delay_explanation_data_proto"],
)

go_proto_library(
    name = "delay_explanation_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/delayexplanationdataevent",
    proto = ":delay_explanation_data_proto",
    visibility = ["//visibility:public"],
)

go_proto_library(
    name = "crew_activity_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/crewactivitydataevent",
    proto = ":crew_activity_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "crew_activity_data_proto",
    srcs = ["CrewActivityData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "crew_activity_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":crew_activity_data_proto"],
)

go_proto_library(
    name = "crew_transition_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/crewtransitiondataevent",
    proto = ":crew_transition_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "crew_transition_data_proto",
    srcs = ["CrewTransitionData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "crew_transition_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":crew_transition_data_proto"],
)

go_proto_library(
    name = "crew_prep_end_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/crewprependdataevent",
    proto = ":crew_prep_end_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "crew_prep_end_data_proto",
    srcs = ["CrewPrepEndData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "crew_prep_end_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":crew_prep_end_data_proto"],
)

go_proto_library(
    name = "crew_shift_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/crewshiftdataevent",
    proto = ":crew_shift_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "crew_shift_data_proto",
    srcs = ["CrewShiftData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "crew_shift_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":crew_shift_data_proto"],
)

go_proto_library(
    name = "infrastructure_manager_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/infrastructuremanagerdataevent",
    proto = ":infrastructure_manager_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "infrastructure_manager_data_proto",
    srcs = ["InfrastructureManagerData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "infrastructure_manager_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":infrastructure_manager_data_proto"],
)

go_proto_library(
    name = "country_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/countrydataevent",
    proto = ":country_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "country_data_proto",
    srcs = ["CountryData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "country_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":country_data_proto"],
)

go_proto_library(
    name = "railway_undertaking_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/railwayundertakingdataevent",
    proto = ":railway_undertaking_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "railway_undertaking_data_proto",
    srcs = ["RailwayUndertakingData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "railway_undertaking_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":railway_undertaking_data_proto"],
)

go_proto_library(
    name = "stays_in_station_event_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/staysinstationevent",
    proto = ":stays_in_station_event_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "stays_in_station_event_proto",
    srcs = ["StaysInStationEvent.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "stays_in_station_event_java_proto",
    visibility = ["//visibility:public"],
    deps = [":stays_in_station_event_proto"],
)

go_proto_library(
    name = "wagon_train_run_data_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/wagontrainrundataevent",
    proto = ":wagon_train_run_data_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "wagon_train_run_data_proto",
    srcs = ["WagonTrainRunData.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "wagon_train_run_data_java_proto",
    visibility = ["//visibility:public"],
    deps = [":wagon_train_run_data_proto"],
)

go_proto_library(
    name = "no_matching_planned_train_data_event_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/nomatchingplannedtraindataevent",
    proto = ":no_matching_planned_train_data_event_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "no_matching_planned_train_data_event_proto",
    srcs = ["NoMatchingPlannedTrainDataEvent.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "no_matching_planned_train_data_event_java_proto",
    visibility = ["//visibility:public"],
    deps = [":no_matching_planned_train_data_event_proto"],
)

go_proto_library(
    name = "count_of_trains_at_station_event_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/countoftrainsatstationevent",
    proto = ":count_of_trains_at_station_event_proto",
    visibility = ["//visibility:public"],
)

proto_library(
    name = "count_of_trains_at_station_event_proto",
    srcs = ["CountOfTrainsAtStationEvent.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

java_proto_library(
    name = "count_of_trains_at_station_event_java_proto",
    visibility = ["//visibility:public"],
    deps = [":count_of_trains_at_station_event_proto"],
)

proto_library(
    name = "event_proto",
    srcs = ["Event.proto"],
    visibility = ["//visibility:public"],
    deps = [
        ":checkpoint_data_proto",
        ":country_data_proto",
        ":crew_activity_data_proto",
        ":crew_prep_end_data_proto",
        ":crew_shift_data_proto",
        ":crew_transition_data_proto",
        ":delay_explanation_data_proto",
        ":gps_trip_update_proto",
        ":infrastructure_manager_data_proto",
        ":live_train_data_proto",
        ":location_data_proto",
        ":planned_train_data_proto",
        ":predicted_train_data_proto",
        ":railway_undertaking_data_proto",
        ":station_data_proto",
        ":train_information_data_proto",
        ":vehicle_data_proto",
        ":weather_data_proto",
        ":count_of_trains_at_station_event_proto",
        "//models/constants:topic_proto",
    ],
)

go_proto_library(
    name = "event_go_proto",
    importpath = "github.com/bptlab/cepta/models/events/event",
    proto = ":event_proto",
    visibility = ["//visibility:public"],
    deps = [
        ":checkpoint_data_go_proto",
        ":country_data_go_proto",
        ":crew_activity_data_go_proto",
        ":crew_prep_end_data_go_proto",
        ":crew_shift_data_go_proto",
        ":crew_transition_data_go_proto",
        ":delay_explanation_data_go_proto",
        ":gps_trip_update_go_proto",
        ":infrastructure_manager_data_go_proto",
        ":live_train_data_go_proto",
        ":location_data_go_proto",
        ":planned_train_data_go_proto",
        ":predicted_train_data_go_proto",
        ":railway_undertaking_data_go_proto",
        ":station_data_go_proto",
        ":train_information_data_go_proto",
        ":vehicle_data_go_proto",
        ":weather_data_go_proto",
        "count_of_trains_at_station_event_go_proto",
        "//models/constants:topic_go_proto",
    ],
)

java_proto_library(
    name = "event_java_proto",
    visibility = ["//visibility:public"],
    deps = [":event_proto"],
)

filegroup(
    name = "events",
    srcs = [
        ":checkpoint_data_go_proto",
        ":checkpoint_data_java_proto",
        ":country_data_go_proto",
        ":country_data_java_proto",
        ":crew_activity_data_go_proto",
        ":crew_activity_data_java_proto",
        ":crew_prep_end_data_go_proto",
        ":crew_prep_end_data_java_proto",
        ":crew_shift_data_go_proto",
        ":crew_shift_data_java_proto",
        ":crew_transition_data_go_proto",
        ":crew_transition_data_java_proto",
        ":delay_explanation_data_go_proto",
        ":delay_explanation_data_java_proto",
        ":gps_trip_update_go_proto",
        ":gps_trip_update_java_proto",
        ":infrastructure_manager_data_go_proto",
        ":infrastructure_manager_data_java_proto",
        ":live_train_data_go_proto",
        ":live_train_data_java_proto",
        ":location_data_go_proto",
        ":location_data_java_proto",
        ":no_matching_planned_train_data_event_go_proto",
        ":no_matching_planned_train_data_event_java_proto",
        ":planned_train_data_go_proto",
        ":planned_train_data_java_proto",
        ":predicted_train_data_go_proto",
        ":predicted_train_data_java_proto",
        ":railway_undertaking_data_go_proto",
        ":railway_undertaking_data_java_proto",
        ":station_data_go_proto",
        ":station_data_java_proto",
        ":stays_in_station_event_go_proto",
        ":stays_in_station_event_java_proto",
        ":train_information_data_go_proto",
        ":train_information_data_java_proto",
        ":vehicle_data_go_proto",
        ":vehicle_data_java_proto",
        ":wagon_train_run_data_go_proto",
        ":wagon_train_run_data_java_proto",
        ":weather_data_go_proto",
        ":weather_data_java_proto",
        ":count_of_trains_at_station_event_proto",
        ":count_of_trains_at_station_event_go_proto",
    ],
    visibility = ["//visibility:public"],
)
