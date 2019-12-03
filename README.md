CEPTA - Complex Event Processing Transport Analysis

[![Build Status](https://travis-ci.com/bptlab/cepta.svg?branch=master)](https://travis-ci.com/bptlab/cepta)
![GitHub](https://img.shields.io/github/license/bptlab/cepta)

The open-source *CEPTA* project aims to examine the applicability of
modern (complex) stream processing 
techniques in the context of intermodal transportation.
The project is under active development and will regularly 
push updates to the [demo instance](https://bpt-lab.org/cepta). 

#### Building
To build the entire project with all modules run
```bash
mvn clean package
```
To build only some modules, run
```bash
mvn mvn -pl <MODULE> -am clean package
mvn mvn -pl backend -am clean package  # Example
```
For a list of modules, check out the root level `pom.xml`.

#### Testing
```bash
mvn test
mvn mvn -pl <MODULE> test  # only test one module
``` 

#### Deployment
The project uses `docker` and `docker-compose` for deployment.
For instructions see `deployment/dev` or `deployment/prod` respectively.
