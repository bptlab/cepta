## CEPTA development deployment

Provides a docker compose file for deploying a local development environment including flink, kafka and other components.

#### Overview
The deployment is configured in a modular fashion to aid 
understanding and allow simple starting and stopping of independent sets
of services during development. Additionally, the configuration
is intended to be ubiquitous for the client host running it, so one 
can interact with all services without the need for proxies.

#### Prerequisites
- `docker` (see the cheat sheet)
- `docker-compose` (see the cheat sheet)

#### Start and stop the development cluster
__Note:__ Before starting the development cluster, make sure 
you do __not have local services with conflicting ports up and running__.
The dev cluster will expose many services default ports, so quit
your own running instances or see _Modularization_ down below.

Start and stop the local cluster using the convenience scripts:
```bash
deployment/dev/devenv.sh up
deployment/dev/devenv.sh down
```

**Note**: If you use **macOS**, the envoy proxy cannot route to services running on your
host machine in host network mode. Therefore, you must set `ENVOY_HOST` to
[docker.internal](docker.internal) and start with:
```bash
ENVOY_HOST=docker.internal deployment/dev/devenv.sh up
```

If any changes were made to the containers, rebuild with
```bash
deployment/dev/devenv.sh down
deployment/dev/devenv.sh build --parallel
deployment/dev/devenv.sh up
```
If any configurations are not applied you might need to delete 
the docker volumes and force recreation
```
deployment/dev/devenv.sh up --force-recreate --always-recreate-deps --renew-anon-volumes
```
Sometimes it is enough to force a recreate of kafka and zookeeper
```
deployment/dev/devenv.sh up --force-recreate zookeeper kafka
```

#### What to do now?
After starting the dev environment, visit 
[http://localhost:8090](http://localhost:8090) to see an overview 
of all the services and their ports. (*Note*: The ports are meant for 
reference. Not every service exposes a web interface.)

You can then visit the Frontent on [http://localhost:80](http://localhost:80).

Development login: `cepta@cepta.org` Password: `cepta` 

#### Starting with data
Some of the data is not meant for public distribution and is kept private.
However, the project strives for adaptability to new sources of data.
If you are a member of the CEPTA project, have a look at some of the private repositories 
on the [schema information](https://gitlab.hpi.de/cepta/meta_schema),
[our internal gitlab](https://gitlab.hpi.de/cepta) which includes repositories for the 
individual data collections and their loading scripts.

In the current state of the project the to be processed events are generated.
There for we use our [Mongo Importer](https://github.com/romnnn/mongoimport)https://github.com/romnnn/mongoimport)
We use a Bazel Build setup to import the models of cepta so they can be replayed form a Replay Mongodb instance.

Example BUILD file for the import of Weather data:
``` 
load("@bazel_gazelle//:def.bzl", "gazelle")
load("@io_bazel_rules_go//go:def.bzl", "go_binary")

gazelle(name = "gazelle")

go_binary(
    name = "load",
    srcs = [
        "load.go",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_bptlab_cepta//models/events:weather_data_go_proto",
        "@com_github_golang_protobuf//ptypes:go_default_library",
        "@com_github_golang_protobuf//ptypes/struct:go_default_library",
        "@com_github_golang_protobuf//ptypes/timestamp:go_default_library",
        "@com_github_romnnn_bsonpb//:go_default_library",
        "@com_github_romnnn_configo//:go_default_library",
        "@com_github_romnnn_mongoimport//:go_default_library",
        "@com_github_romnnn_mongoimport//files:go_default_library",
        "@com_github_romnnn_mongoimport//loaders:go_default_library",
        "@com_github_sirupsen_logrus//:go_default_library",
        "@com_github_skydivin4ng3l_datatypeconverter//:go_default_library",
        "@com_github_urfave_cli_v2//:go_default_library",
    ],
)
```

#### How do events enter the system: Replay of historic Data
The replayer module will replay by default from the internal docker mongodb instance from a `replay` database. 
Here the replayer expects to find the history data collections.
More information about the replayer can be found here [Replayer Readme](../../auxiliary/producers/replayer/README.md)

#### Modularization
This launches all the default services.
If you want to start only some services, pass the names of the services 
to the `./devenv.sh up` command. For example:
```bash
cd deployment/dev
./devenv.sh up osiris, anubis, envoy
```
