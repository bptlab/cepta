load("@io_bazel_rules_go//go:def.bzl", "go_library")
load("@io_bazel_rules_go//proto:def.bzl", "go_proto_library")
load("@rules_proto_grpc//github.com/grpc/grpc-web:defs.bzl", "ts_grpc_compile", "commonjs_dts_grpc_compile", "closure_grpc_compile")
load("@rules_proto_grpc//nodejs:defs.bzl", "nodejs_proto_compile")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_typescript_proto//:index.bzl", "typescript_proto_library")

# User management 
proto_library(
    name = "user_management_proto",
    srcs = ["user_management.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

go_proto_library(
    name = "user_management_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/user_management",
    proto = ":user_management_proto",
    visibility = ["//visibility:public"],
)

# Authentication

proto_library(
    name = "authentication_proto",
    srcs = ["authentication.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

go_proto_library(
    name = "authentication_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/authentication",
    proto = ":authentication_proto",
    visibility = ["//visibility:public"],
)

# Replayer
proto_library(
    name = "replayer_proto",
    srcs = ["replayer.proto"],
    visibility = ["//visibility:public"],
    deps = ["@com_google_protobuf//:timestamp_proto"],
)

go_proto_library(
    name = "replayer_go_proto",
    compilers = ["@io_bazel_rules_go//proto:go_grpc"],
    importpath = "github.com/bptlab/cepta/models/grpc/replayer",
    proto = ":replayer_proto",
    visibility = ["//visibility:public"],
)

typescript_proto_library(
  name = "replayer_ts_proto",
  proto = ":replayer_proto",
)

ts_grpc_compile(
    name = "replayer_ts_grpc_proto",
    deps = [":replayer_proto"],
)

commonjs_dts_grpc_compile(
    name = "replayer_commonjs_dts_grpc_proto",
    deps = ["replayer_proto"],
)

closure_grpc_compile(
    name = "replayer_closure_grpc_proto",
    deps = ["replayer_proto"],
)

nodejs_proto_compile(
    name = "replayer_nodejs_proto",
    deps = ["replayer_proto"],
)

genrule(
    name = "please",
    # srcs = glob(["git/**"]),
    srcs = [
        ":replayer_ts_grpc_proto"
    ],
    outs = [
        "google/protobuf/timestamp_pb.d.ts",
        # "//anubis/src/generated/google/protobuf/timestamp_pb.d.ts",
        # "//anubis/src/generated/models/grpc/replayer_pb.d.ts",
        # "//anubis/src/generated/models/grpc/ReplayerServiceClientPb.ts",
    ],
    # folder contain this BUILD.bazel file is tool which will be symbol linked, we use cd -P to get to the physical path
    # cmd = "echo 'setup pre-commit.sh' > $(OUTS) && cd -P tools && ./path/to/your-script.sh",
    cmd = "echo $(SRCS) && echo $(OUTS) && echo $(RULEDIR) && echo $(@D)",
    local = 1,  # To access sources
    output_to_bindir = 1,
)

go_library(
    name = "go_default_library",
    embed = [":replayer_go_proto"],
    importpath = "github.com/bptlab/cepta/models/grpc/replayer",
    visibility = ["//visibility:public"],
)

filegroup(
    name = "grpc",
    srcs = [
        # ":replayer_ts_grpc_proto",
        ":replayer_commonjs_dts_grpc_proto",
        "replayer_nodejs_proto",
        # ":replayer_ts_proto",
        ":replayer_go_proto",
    ],
    visibility = ["//visibility:public"],
)