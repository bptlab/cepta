## Yes, an entire entry dedicated to kafka problems.
#### We _love_ kafka! :rage:

Tip: If you want to debug messages and topics, use `kafdrop` from our docker dev tooling at [localhost:9001](http://localhost:9001).

## Leadership election bla bla
> _"There is no leader for this topic-partition as we are in the middle of a leadership election"_

This seems to happen because the `kafka` broker gets assigned an automatic broker-id. So if you restart the container when it registers with `zookeeper` it will get the next auto-generated ID. This should be evident in the logs (you'll see an initial broker ID of 1001, then 1002, 1003 etc). However, any topics you have already created will be assigned to now non-existing broker IDs, so the leader will be unavailable.

Known workarounds:
  - Delete all topics (and hope they really will be deleted):
    ```bash
    ./kafka-topics.sh --zookeeper localhost:2181 --delete --topic '.*'
    ```
  - Recreate all containers so that zookeeper might forget it ever even existed:
    ```
    deployment/dev/devenv.sh up --force-recreate kafka zookeeper kafdrop kafka-manager
    ```