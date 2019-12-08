## CEPTA production deployment

Provides a docker compose file for deploying a 
production environment for the CEPTA platform.

#### Overview
The deployment is configured to be used in a production environment.
Other than the `dev` deployment, this configuration only exposes
a single ingress port and enforces stronger isolation.

#### Prerequisites
- `docker` (see the cheat sheet)
- `docker-compose` (see the cheat sheet)

#### Start and stop the development cluster
__Note:__ Before starting the production cluster, make sure 
you do not have any local service running on port 80.

```bash
cd deployment/prod
docker-compose up
docker-compose down
```
If any changes were made to the containers, rebuild with
```bash
cd deployment/prod
docker-compose down
docker-compose build --parallel
docker-compose up
```
If any configurations are not applied you might need to delete 
the docker volumes and force recreation
```bash
cd deployment/prod
docker-compose up --force-recreate --always-recreate-deps --renew-anon-volumes
```
