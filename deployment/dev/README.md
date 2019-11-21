## Cepta development deployment

Provides a docker compose file for deploying a local development environment including flink, kafka and other components.

#### Prerequisites
- `docker` (see the cheat sheet)
- `docker-compose` (see the cheat sheet)

#### Start and stop the development cluster
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

This launches all the default services.
If you want to start only some services, have a look at the
`start.sh` script.
If you only want to start the `postgres` service, 
the command would look like this:
```bash
cd compose
docker-compose -f postgres.compose.yml up
```
