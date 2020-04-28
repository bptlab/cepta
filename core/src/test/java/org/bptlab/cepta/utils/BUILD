load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "id-generator",
    size = "small",
    srcs = ["IDGeneratorTest.java"],
    tags = ["unit"],
    test_class = "org.bptlab.cepta.test.utils.IDGeneratorTest",
    deps = [
        "//core/src/main/java/org/bptlab/cepta/utils:java_default_library",
    ],
)

java_test(
    name = "geo-generator",
    size = "small",
    srcs = ["GeoTest.java"],
    tags = ["unit"],
    test_class = "org.bptlab.cepta.test.utils.GeoTest",
    deps = [
        "//core/src/main/java/org/bptlab/cepta/utils:java_default_library",
        "//core/src/main/java/org/bptlab/cepta/utils/geo:java_default_library",
        "//models/internal/types:coordinate_java_proto",
        "@maven//:org_javatuples_javatuples",
    ],
)

test_suite(
    name = "smoke",
    tags = [
        "-docker",
        "-internal",
    ],
)

test_suite(
    name = "unit",
    tags = [
        "-internal",
        "unit",
    ],
)

test_suite(
    name = "integration",
    tags = [
        "-internal",
        "integration",
    ],
)

test_suite(name = "internal")
