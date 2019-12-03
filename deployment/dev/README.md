## Cepta development deployment

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
you do not have local services with conflicting ports up and running.
The dev cluster will expose many services default ports, so quit
your own running instances or see _Modularization_ down below.

Start and stop the local cluster using the convenience scripts:
```bash
./start.sh
./stop.sh
```
If any changes were made to the containers, rebuild with
```bash
./start.sh --build
```
If any configurations are not applied you might need to delete 
the docker volumes and force recreation
```
/start.sh --force-recreate --always-recreate-deps --renew-anon-volumes
```

#### What to do now?
After starting the dev environment, navigate to 
[http://localhost:80](http://localhost:80) to see an overview 
of all the services and their ports.

#### Modularization

This launches all the default services.
If you want to start only some services, have a look at the
`start.sh` script.
If you only want to start the `postgres` service, 
the command would look like this:
```bash
cd compose
docker-compose -f postgres.compose.yml up
```
