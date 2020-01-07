## CEPTA production deployment

Provides a docker compose file for deploying *CEPTA*.

#### Overview
The deployment is configured to provide a single point of entry for the CEPTA application.
It intentionally exposes only the main ingress port and uses reverse proxying and 
container internal communication only.

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
deployment/prod/prodenv.sh up
deployment/prod/prodenv.sh down
```

If any changes were made to the containers, rebuild with
```bash
deployment/prod/prodenv.sh down
deployment/prod/prodenv.sh build --parallel
deployment/prod/prodenv.sh up
```
If any configurations are not applied you might need to delete 
the docker volumes and force recreation
```
deployment/prod/prodenv.sh up --force-recreate --always-recreate-deps --renew-anon-volumes
```

#### What to do now?
After starting the production environment, visit the application at 
[http://localhost:8080](http://localhost:80).

#### Starting with data
Some of the data is not meant for public distribution and is kept private.
However, the project strives for adaptability to new sources of data.
If you are a member of the CEPTA project, have a look at some of the private repositories 
on the [schema information](https://gitlab.hpi.de/cepta/meta_schema),
[data](https://gitlab.hpi.de/cepta/synfioo-data) 
and [utilities for loading data](https://gitlab.hpi.de/cepta/bp-data-helper).

#### Import train data into the database
1. Clone [utilities for loading data](https://gitlab.hpi.de/cepta/bp-data-helper).
2. Start postgres and pgadmin: `/prodenv.sh up postgres pgadmin`
3. Wait for `./bp.sh $(realpath ./selected-data/) $(realpath ./hooks/metaschema-post-load)` to finish importing
4. After import, the `postgres` database should persist the imported data.
5. Stop postgres and start the entire cluster: `/prodenv.sh down && /prodenv.sh up`
6. Optional: Explore the data using `pgadmin` (check the [dev cockpit](http://localhost:8080))

#### Modularization
This launches all the default services.
If you want to start only some services, pass the names of the services 
to the `./prodenv.sh up` command. For example:
```bash
deployment/prod/prodenv.sh up osiris, anubis, envoy
```
