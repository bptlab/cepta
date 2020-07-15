<p align="center">
  <img style="display:inline-block" width="400px" src="https://user-images.githubusercontent.com/19370911/80150874-706f2e80-85b9-11ea-92e8-2a4bf79c0314.png">
</p>

CEPTA - Complex Event Processing Transportation Analysis

[![Build Status](https://travis-ci.com/bptlab/cepta.svg?branch=master)](https://travis-ci.com/bptlab/cepta)
![GitHub](https://img.shields.io/github/license/bptlab/cepta)
[![Release](https://img.shields.io/github/release/bptlab/cepta)](https://github.com/bptlab/cepta/releases/latest)

The open-source *CEPTA* project aims to examine the applicability of
modern (complex) event processing 
techniques in the context of intermodal transportation.
The project is under active development and will regularly 
push updates to the [demo instance](https://bpt-lab.org/cepta).

For more information, see [bptlab.github.io/cepta/](https://bptlab.github.io/cepta/).

![Screenshot](web/images/screenshot-light.png)

#### Building
To build all executables of the entire project:
```bash
bazel build //:cepta
```
To build only a specific module or executable:
```bash
bazel build //auxiliary/producers/replayer  # Example
```

#### Running
To run a specific executable:
```bash
bazel run //auxiliary/producers/replayer -- --port 8080  # Example
```
To run mutiple specific executables (prevents locking) use run.sh instead of bazel run:
```bash
./run.sh //auxiliary/producers/replayer -- --port 8080  # Example
```



#### Testing
```bash
bazel test :all
bazel test //core:core-tests  # Only test core
``` 

#### Deployment
The project uses `docker` and `docker-compose` for deployment.
For instructions see `docs/deployment/dev.md` or `docs/deployment/prod.md` respectively.

Summary: 
To run the latest production version, run 
```bash
CEPTA_VERSION="v0.5.0" docker-compose -f deployment/prod/docker-compose.yml up
```

To build and run the latest development version, run 
```bash
BUILD=1 ./deployment/dev/devenv.sh up
```

To run specific containers (single or multiple) run:
```bash
./deployment/dev/devenv.sh up args...
```

The containers that can be used as args can be found in /deployment/dev/compose for example: envoy

Have you never build the docker images before than run following instead:
```
BUILD=1 ./deployment/dev/devenv.sh up args...
```

#### Deployment on Mac
Seeing an error like this while starting the docker container:  
`standard_init_linux.go:211: exec user process caused "exec format error"`

MacOs needs a slight configuration to the build stage. The docker images that are build with bazel have to be build for linux to run in the docker environment. Therefore you have to build the images like this: 

```
bazel run //osiris/usermgmt:build-image --platforms=@io_bazel_rules_go//go/toolchain:linux_amd64
```
