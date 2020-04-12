CEPTA - Complex Event Processing Transportation Analysis

[![Build Status](https://travis-ci.com/bptlab/cepta.svg?branch=master)](https://travis-ci.com/bptlab/cepta)
![GitHub](https://img.shields.io/github/license/bptlab/cepta)
[![Release](https://img.shields.io/github/release/bptlab/cepta)](https://github.com/bptlab/cepta/releases/latest)

The open-source *CEPTA* project aims to examine the applicability of
modern (complex) event processing 
techniques in the context of intermodal transportation.
The project is under active development and will regularly 
push updates to the [demo instance](https://bpt-lab.org/cepta). 

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

#### Testing
```bash
bazel test :all
bazel test //core:core-tests  # Only test core
``` 

#### Deployment
The project uses `docker` and `docker-compose` for deployment.
For instructions see `deployment/dev` or `deployment/prod` respectively.

Summary: To run the latest version, run 
```bash
CEPTA_VERSION="v0.1.0" docker-compose -f deployment/prod/docker-compose.yml up
```