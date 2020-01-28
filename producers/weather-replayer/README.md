## Train data replayer

This service executable connects to `postgres` and sends events
from the database ordered by their time of creation to 
simulate a real time event stream at varying frequencies.

```bash
cd cepta
./run.sh producers/train-replayer/ --grpc-port=9005 --db-host=localhost --must-match='train_id=47298333'
```

__Note__: The service expects to find data in the database.
Please make sure to import data as described in the [deployment
guide](https://github.com/bptlab/cepta/blob/master/deployment/dev/README.md).