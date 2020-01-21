## CEPTA production deployment

Provides a docker compose file for deploying *CEPTA* in production.

#### Overview
The deployment is configured to provide a single point of entry for the CEPTA application.
It intentionally exposes only the main ingress port and uses reverse proxying and 
container internal communication only.

#### Prerequisites
- `docker` (see the cheat sheet)
- `docker-compose` (see the cheat sheet)
- Access to the `bptlab` docker registry where our microservice's containers are published

#### Start and stop the production cluster
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
[localhost:80](http://localhost:80).

#### More information
For details regarding the data see the development deployment notes.