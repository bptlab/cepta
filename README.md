CEPTA - Complex Event Processing Transport Analysis

[![Build Status](https://travis-ci.com/bptlab/cepta.svg?branch=master)](https://travis-ci.com/bptlab/cepta)
![GitHub](https://img.shields.io/github/license/bptlab/cepta)

The open-source *CEPTA* project aims to examine the applicability of
modern (complex) event processing 
techniques in the context of intermodal transportation.
The project is under active development and will regularly 
push updates to the [demo instance](https://bpt-lab.org/cepta). 

#### Building
To build the entire project with all modules:
```bash
mvn clean package
```
To `clean compile` a module, you can use the provided convenience script:
```bash
./run.sh <MODULE>
./run.sh core  # Example
./run.sh anubis --server.port=8085  # Example with arguments
```

To build only some modules:
```bash
mvn -am -pl <MODULE> -am clean package
mvn -am -pl backend -am clean package  # Example
```
For a list of modules, check out the root level `pom.xml`.

#### Testing
```bash
mvn test
mvn -am -pl <MODULE> test  # only test one module
mvn -am -pl backend test  # Example
``` 

#### Deployment
The project uses `docker` and `docker-compose` for deployment.
For instructions see `deployment/dev` or `deployment/prod` respectively.
