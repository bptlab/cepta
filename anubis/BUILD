load("@npm//webpack-cli:index.bzl", webpack = "webpack_cli")
load("//:rules.bzl", "webpack_bundle")
load("@npm//typescript:index.bzl", "tsc")

genrule(
    name = "build",
    srcs = [
        # "anubis",
        # "//some:files",  # a filegroup with multiple files in it ==> $(locations)
        # "//other:gen",   # a genrule with a single output ==> $(location)
        # "@npm//:node_modules",
        "@npm//:node_modules",
    ],
    # outs = ["dist"],
    outs = [],
    # cmd = "cat $(locations //some:files) $(location //other:gen) > $@",
    cmd = "cd anubis/ && npm run build --prefix anubis",
)

tsc(
    name = "compile",
    outs = ["index.js"],
    args = [
        "$(execpath index.tsx)",
        "$(execpath types.d.ts)",
        "--outDir",
        "$(RULEDIR)",
        "--lib",
        "es2015,dom",
        "--jsx",
        "vue",
    ],
    data = glob(["src/**/*"]) + [
        "main.ts",
        "tsconfig.json",
        "types.d.ts",
        "shims-vue.d.ts",
        "shims-tsx.d.ts",
        "@npm//@types",
        "@npm//csstype",
    ],
)

webpack_bundle(
    name = "lol",
    config = "anubis/node_modules/@vue/cli-service/webpack.config.js",
    srcs = glob(["src/**/*"]) + [
        # "src/main.ts",
        "vue.config.js",
        "tsconfig.json",
        "package.json",
        # "styles.css",
        # "webpack.config.js",
        # "@npm//:node_modules/@vue/cli-service/webpack.config.js",
        "@npm//:node_modules",
    ],
)

webpack(
    name = "bundle",
    outs = ["app.bundle.js"],
    args = [
        "$(execpath src/main.ts)",
        # "--context",
        # "$(execpath src/)",
        # "anubis/src",
        "--config",
        "$(execpath node_modules)/@vue/cli-service/webpack.config.js",
        # "anubis/node_modules/@vue/cli-service/webpack.config.js",
        "-o",
        "$@",
    ],
    data = glob(["src/**/*"]) + [
        # "src/main.ts",
        "vue.config.js",
        "tsconfig.json",
        "package.json",
        # "styles.css",
        # "webpack.config.js",
        # "@npm//:node_modules/@vue/cli-service/webpack.config.js",
        "@npm//:node_modules",
    ],
)

# <projectRoot>/node_modules/@vue/cli-service/webpack.config.js