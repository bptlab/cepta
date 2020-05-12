## Anubis web frontend

## Get started
|Command | Description
|--------|------------
| `npm install` | Installs all dependencies
| `npm run serve` | Run a local dev version with hot reload
| `npm run build` | Compile optimized static files

## Testing
|Command | Description
|--------|------------
| `npm run lint` | Lint and check for errors
| `npm run test:unit` | Run unit tests
| `npm run test:e2e` | Run end to end tests

## Troubleshooting macOS

On macOS you should run as root to avoid permission denied errors:
```
sudo npm run serve
```
You might have to execute the command twice in order to find the folder `./src/generated/protobuf`

## Logging in

Be sure to have our authentication and usermanagement microservices running:

- in /cepta/deployment/dev:
```
BUILD=1 ./devenv.sh up kafka zookeeper envoy replayer auth mongodb usermgmt 
```

- now, run the frontend as described above.

- now go to http://localhost:80 and login with these credentials:

    user: cepta@cepta.org
    
    password: cepta