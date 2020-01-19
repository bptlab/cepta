load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "io_bazel_rules_go",
    urls = [
        "https://storage.googleapis.com/bazel-mirror/github.com/bazelbuild/rules_go/releases/download/v0.21.0/rules_go-v0.21.0.tar.gz",
        "https://github.com/bazelbuild/rules_go/releases/download/v0.21.0/rules_go-v0.21.0.tar.gz",
    ],
    sha256 = "b27e55d2dcc9e6020e17614ae6e0374818a3e3ce6f2024036e688ada24110444",
)

load("@io_bazel_rules_go//go:deps.bzl", "go_rules_dependencies", "go_register_toolchains")

go_rules_dependencies()

go_register_toolchains()

http_archive(
    name = "bazel_gazelle",
    urls = [
        "https://storage.googleapis.com/bazel-mirror/github.com/bazelbuild/bazel-gazelle/releases/download/v0.19.1/bazel-gazelle-v0.19.1.tar.gz",
        "https://github.com/bazelbuild/bazel-gazelle/releases/download/v0.19.1/bazel-gazelle-v0.19.1.tar.gz",
    ],
    sha256 = "86c6d481b3f7aedc1d60c1c211c6f76da282ae197c3b3160f54bd3a8f847896f",
)

load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies", "go_repository")

gazelle_dependencies()

go_repository(
    name = "co_honnef_go_tools",
    importpath = "honnef.co/go/tools",
    sum = "h1:XJP7lxbSxWLOMNdBE4B/STaqVy6L73o0knwj2vIlxnw=",
    version = "v0.0.0-20190102054323-c2f93a96b099",
)

go_repository(
    name = "com_github_burntsushi_toml",
    importpath = "github.com/BurntSushi/toml",
    sum = "h1:WXkYYl6Yr3qBf1K79EBnL4mak0OimBfB0XUf9Vl28OQ=",
    version = "v0.3.1",
)

go_repository(
    name = "com_github_client9_misspell",
    importpath = "github.com/client9/misspell",
    sum = "h1:ta993UF76GwbvJcIo3Y68y/M3WxlpEHPWIGDkJYwzJI=",
    version = "v0.3.4",
)

go_repository(
    name = "com_github_danielvladco_go_proto_gql_pb",
    importpath = "github.com/danielvladco/go-proto-gql/pb",
    sum = "h1:zEQZroAJx7kzzliJqHa+KfNMNneVK3caZx0juTxH6Oo=",
    version = "v0.6.0",
)

go_repository(
    name = "com_github_gogo_protobuf",
    importpath = "github.com/gogo/protobuf",
    sum = "h1:/s5zKNz0uPFCZ5hddgPdo2TK2TVrUNMn0OOX8/aZMTE=",
    version = "v1.2.1",
)

go_repository(
    name = "com_github_golang_glog",
    importpath = "github.com/golang/glog",
    sum = "h1:VKtxabqXZkF25pY9ekfRL6a582T4P37/31XEstQ5p58=",
    version = "v0.0.0-20160126235308-23def4e6c14b",
)

go_repository(
    name = "com_github_golang_mock",
    importpath = "github.com/golang/mock",
    sum = "h1:G5FRp8JnTd7RQH5kemVNlMeyXQAztQ3mOWV95KxsXH8=",
    version = "v1.1.1",
)

go_repository(
    name = "com_github_golang_protobuf",
    importpath = "github.com/golang/protobuf",
    sum = "h1:YF8+flBXS5eO826T4nzqPrxfhQThhXl0YzfuUPu4SBg=",
    version = "v1.3.1",
)

go_repository(
    name = "com_github_kisielk_errcheck",
    importpath = "github.com/kisielk/errcheck",
    sum = "h1:ZqfnKyx9KGpRcW04j5nnPDgRgoXUeLh2YFBeFzphcA0=",
    version = "v1.1.0",
)

go_repository(
    name = "com_github_kisielk_gotool",
    importpath = "github.com/kisielk/gotool",
    sum = "h1:AV2c/EiW3KqPNT9ZKl07ehoAGi4C5/01Cfbblndcapg=",
    version = "v1.0.0",
)

go_repository(
    name = "com_google_cloud_go",
    importpath = "cloud.google.com/go",
    sum = "h1:e0WKqKTd5BnrG8aKH3J3h+QvEIQtSUcf2n5UZ5ZgLtQ=",
    version = "v0.26.0",
)

go_repository(
    name = "in_gopkg_check_v1",
    importpath = "gopkg.in/check.v1",
    sum = "h1:yhCVgyC4o1eVCa2tZl7eS0r+SDo693bJlVdllGtEeKM=",
    version = "v0.0.0-20161208181325-20d25e280405",
)

go_repository(
    name = "in_gopkg_yaml_v2",
    importpath = "gopkg.in/yaml.v2",
    sum = "h1:ZCJp+EgiOT7lHqUV2J862kp8Qj64Jo6az82+3Td9dZw=",
    version = "v2.2.2",
)

go_repository(
    name = "org_golang_google_appengine",
    importpath = "google.golang.org/appengine",
    sum = "h1:igQkv0AAhEIvTEpD5LIpAfav2eeVO9HBTjvKHVJPRSs=",
    version = "v1.1.0",
)

go_repository(
    name = "org_golang_google_genproto",
    importpath = "google.golang.org/genproto",
    sum = "h1:Nw54tB0rB7hY/N0NQvRW8DG4Yk3Q6T9cu9RcFQDu1tc=",
    version = "v0.0.0-20180817151627-c66870c02cf8",
)

go_repository(
    name = "org_golang_google_grpc",
    importpath = "google.golang.org/grpc",
    sum = "h1:TrBcJ1yqAl1G++wO39nD/qtgpsW9/1+QGrluyMGEYgM=",
    version = "v1.19.1",
)

go_repository(
    name = "org_golang_x_lint",
    importpath = "golang.org/x/lint",
    sum = "h1:x/bBzNauLQAlE3fLku/xy92Y8QwKX5HZymrMz2IiKFc=",
    version = "v0.0.0-20181026193005-c67002cb31c3",
)

go_repository(
    name = "org_golang_x_net",
    importpath = "golang.org/x/net",
    sum = "h1:g9qWBGx4puODJTMVyoPrpoxPFgVGd+z1DZwjfRu4d0I=",
    version = "v0.0.0-20180826012351-8a410e7b638d",
)

go_repository(
    name = "org_golang_x_oauth2",
    importpath = "golang.org/x/oauth2",
    sum = "h1:vEDujvNQGv4jgYKudGeI/+DAX4Jffq6hpD55MmoEvKs=",
    version = "v0.0.0-20180821212333-d2e6202438be",
)

go_repository(
    name = "org_golang_x_sync",
    importpath = "golang.org/x/sync",
    sum = "h1:wMNYb4v58l5UBM7MYRLPG6ZhfOqbKu7X5eyFl8ZhKvA=",
    version = "v0.0.0-20180314180146-1d60e4601c6f",
)

go_repository(
    name = "org_golang_x_sys",
    importpath = "golang.org/x/sys",
    sum = "h1:Ve1ORMCxvRmSXBwJK+t3Oy+V2vRW2OetUQBq4rJIkZE=",
    version = "v0.0.0-20180830151530-49385e6e1522",
)

go_repository(
    name = "org_golang_x_text",
    importpath = "golang.org/x/text",
    sum = "h1:g61tztE5qeGQ89tm6NTjjM9VPIm088od1l6aSorWRWg=",
    version = "v0.3.0",
)

go_repository(
    name = "org_golang_x_tools",
    importpath = "golang.org/x/tools",
    sum = "h1:qMK98NmNCRVDIYFycQ5yVRkvgDUFfdP8Ip4KqmDEB7g=",
    version = "v0.0.0-20190114222345-bf090417da8b",
)
