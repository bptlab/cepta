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
cd deployment/dev
./devenv.sh up
./devenv.sh down
```

If any changes were made to the containers, rebuild with
```bash
cd deployment/dev
./devenv.sh down
./devenv.sh build --parallel
./devenv.sh up
```
If any configurations are not applied you might need to delete 
the docker volumes and force recreation
```
cd deployment/dev
/devenv.sh up --force-recreate --always-recreate-deps --renew-anon-volumes
```

#### What to do now?
After starting the dev environment, navigate to 
[http://localhost:8080](http://localhost:8080) to see an overview 
of all the services and their ports. (*Note*: The ports are meant for 
reference. Not every service exposes a web interface.)

#### Starting with data
Some of the data is not meant for public distribution and is kept private.
However, the project strives for adaptability to new sources of data.
If you are a member of the CEPTA project, consider some of the private repositories 
on the [schema information](https://gitlab.hpi.de/cepta/meta_schema),
[data](https://gitlab.hpi.de/cepta/synfioo-data) 
and [utilities for loading data](https://gitlab.hpi.de/cepta/bp-data-helper).

#### Modularization
This launches all the default services.
If you want to start only some services, pass the names of the services 
to the `./devenv.sh up` command. For example:
```bash
cd deployment/dev
./devenv.sh up osiris, anubis, envoy
```
