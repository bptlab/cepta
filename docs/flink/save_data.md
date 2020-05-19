# Introduction

Since we alot of correlated data to work with (trains with ids, location ids etc.), we would like to save them in an equally correlated way. One option to do this would be using flink. 

This page should give you a rough idea on how flink saves data internally and wether or not we can use flink to save our correlated data. If you simply want to read the conclusion, skip to the end.

# Flinks internal state

Flink keeps an internal state for everything it needs during program execution. This includes user defined variables, open windows, aggregates and so on. All these values are saved inside key/value pairs. 

State can also be persisted by enabling checkpointing.

There are also different backends that affect the amount of internal space aswell as the speed:
	
1. MemoryStateBackend

	Everything is inside the Java heap. This defaults to 5 MB of memory, 
but can be configured to be
as big as the flink JobManagers memory


2. FsStateBackend

	Everything is situated in the FileSystem, Flink keeps metadata ready to read from there.
	While the possible space is much larger, it might also be subject to data reading speeds.
	

3. RocksDBStateBackend

	Uses a RocksDB Database to keep all key/value pairs. Can keep alot of space (total available memory), is perhaps slightly slower due to having to communicate between flink and the database (object de-/serialization)

If you want some further reading, check out [flinks docs on state backends](https://ci.apache.org/projects/flink/flink-docs-stable/ops/state/state_backends.html) and [on states](https://ci.apache.org/projects/flink/flink-docs-release-1.9/dev/stream/state/index.html).

# Is this method useful for us?

The state's purpose is mainly to store internal Flink variables. I haven't found any approaches for storing complete databases inside of it. It could be possible if we do alot of precomputation on our data but that might be too much effort (so that we do not need to do complex SQL).

For now, a good approach seems to be connecting from Flink to a relational database that holds our data.

# Alternatives

Now, we will have a look at different alternatives to Flinks state.

## TableAPI 

Flink offers a [TableAPI](https://ci.apache.org/projects/flink/flink-docs-stable/dev/table/tableApi.html). This allows doing transformations on streams in a SQL-like way. 
It doesn't appear to be made for doing queries like we need them however, as the TableAPI [is just an API built ontop](https://ci.apache.org/projects/flink/flink-docs-release-1.9/concepts/programming-model.html) of the already existing DataSet and DataStream APIs.

## Asynchronous I/O

Flink offers asynchronous I/O which we could use with a relational database: https://ci.apache.org/projects/flink/flink-docs-stable/dev/stream/operators/asyncio.html
A function could be used to make asynchronous queries to e. g.enrich our data. Due to it being asynchronous we wouldn't have to wait for the queries to finish, allowing us to have a much higher throughput.

A suitable database (client) would thus need to support asynchronous queries.

## which databases could we use?

### RocksDB

- is already used in flink's state backend.
- is highly efficient at saving key/ value pairs, doesn't support relational data apparently
- has java support
- no SQL 


### Postgres

- OpenSource
- lots of java support like JDBC
- also has asynchronous clients like jasync (https://github.com/jasync-sql/jasync-sql)
- vertx (https://vertx.io/docs/vertx-mysql-postgresql-client/java/) is a client that uses jasync, but needs a higher scala version than flink which could lead to problem
- apparently its possible to write from flink to postgre: https://tech.signavio.com/2017/postgres-flink-sink 

# Conclusion

Using Flink's internal state would be possible but would take alot of time to implement. A better approach appears to be using a database that we query asynchronally. An example on how to do the latter can be found [on the wiki page 'query database inside flink'](https://github.com/bptlab/cepta/wiki/Query-database-inside-flink).