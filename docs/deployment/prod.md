## CEPTA production deployment

Provides a docker compose file for deploying *CEPTA* in production.

#### Overview
The deployment is configured to provide a single point of entry for the CEPTA application.
It intentionally exposes only the main ingress port and uses reverse proxying and 
container internal communication only.

#### Prerequisites
- `docker` (see the cheat sheet)
- `docker-compose` (see the cheat sheet)
- Access to the [ceptaorg](https://hub.docker.com/orgs/ceptaorg) docker registry where our microservice's containers are published


#### Publishing the docker containers

We use bazel to build our containers and publish them to the [ceptaorg](https://hub.docker.com/orgs/ceptaorg) organization. Make sure you are logged in to docker using `docker login --username=<your-username>` and have access to the organization. Then run:
```bash
bazel run //:publish  # Build and push all images 
bazel run //core:publish  # Build and push only core 
bazel run //osiris:publish   # Build and push osiris (notification, query ...)
```

See the bazel configuration for more targets.

#### Start and stop the production cluster
Start and stop the local cluster using the convenience scripts:
```bash
deployment/prod/prodenv.sh pull # Make sure to pull the newest prod containers
deployment/prod/prodenv.sh up
deployment/prod/prodenv.sh down
```

#### What to do now?
After starting the production environment you should be able to access the application at 
[localhost:80](http://localhost:80).

#### More information
For details regarding the data see the development deployment notes.