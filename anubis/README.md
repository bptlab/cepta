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
| `npm run test:unit` | Run unittests
| `npm run test:e2e` | Run end to end tests

## Troubleshooting macOS

On macOS you should run as root to avoid permission denied errors:
```
sudo npm run serve
```
You might have to execute the command twice in order to find the folder `./src/generated/protobuf`