# Introduction

In order to correlate all the different events, one approach would be querying a database in order to e. g. find out what location is behind an id, or which previous delays were found on the same track.

This page should give an example on how to query a database from flink in different ways. One way is using a synchronous jdbc connection, another is using the asynchronous driver 'jasync', which will both be documented here in their own sections.

# The example

In this example we will have two tables:

```
Table "public.actor"
  Column  |          Type          | Collation | Nullable | Default 
----------+------------------------+-----------+----------+---------
 id       | integer                |           | not null | 
 name     | character varying(127) |           |          | 
 movie_id | character varying(127) |           |          | 
 role     | character varying(127) |           |          | 
 position | integer                |           |          | 
```

```
Table "public.movie"
 Column |          Type          | Collation | Nullable | Default 
--------+------------------------+-----------+----------+---------
 mid    | character varying(127) |           | not null | 
 title  | character varying(127) |           |          | 
 year   | integer                |           |          | 
```

Each actor played in 1 or more movies. In this example we will correlate the actor.movie_id to the movie.mid and count how many times each actor appeared in the movie table. The actor names will be read from [this file](https://github.com/bptlab/cepta/blob/21-db-for-flink/src/main/java/org/bptlab/dbForFlink/all_actors.txt) for convenience, but we may also just read them from another query or incoming stream.

## Data setup

If you want to experiment yourself or just execute the program, import the data which nside a .sql file, which is basically an exported database we can import by doing the following:

1. download the sample dataset from [here](https://github.com/bptlab/cepta/blob/21-db-for-flink/src/main/java/org/bptlab/dbForFlink/imdb_database_dump.sql)
2. open psql-shell
3. use `\i <path_to_file>/imdb_database_dump.sql` to create a database with filled tables.

If you want to verify the import you may:
- use `\c dbs1_imdb` to change to the dbs1_imdb database you created with the command above
- use `\d` to show all tables
- `\d <table>` will show you the schema of a single table 

- you may also use `\conninfo` to show where your database can be queried (to adjust the connection)

# JDBC

[(here is the complete code)](https://github.com/bptlab/cepta/blob/21-db-for-flink/src/main/java/org/bptlab/dbForFlink/flinkWithJDBC.java).

# jasync

[(here is the complete code)](https://github.com/bptlab/cepta/blob/21-db-for-flink/src/main/java/org/bptlab/dbForFlink/flinkWithJasync.java)

## Dependencies

Add this to your pom.xml:

```
<dependency>
	<groupId>com.github.jasync-sql</groupId>
	<artifactId>jasync-postgresql</artifactId>
	<version>1.0.11</version>
</dependency>
```

This will allow you to create a postgre connection aswell as query that connection.

## creating an asynchronous mapping function

### defining the class

```
class MapActorNamesToAppearancesAsync extends RichAsyncFunction<String, Tuple2<String, Integer>> {
}
```

Inheriting from a RichAsyncFunction will give us access to the  `open()` and `close()` functions that will be executing before and after each executing on the execution nodes. We can use this to create our conection.

### connecting to the database

Add the following variable to your function:

```
//this must be set to transient, as flink will otherwise try to serialize it which it is not
private transient ConnectionPool<PostgreSQLConnection> connection;
```
This will allow us to have a shared ConnectionPool.
_Be aware this creates a ConnectionPool on each execution node, so you will have parrallism-many!_

Now we create the connectionPool:

```
public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        //the Configuration class must be from flink, it will give errors when jasync's Configuration is taken
        //open should be called before methods like map() or join() are executed
        super.open(parameters);
        ConnectionPoolConfigurationBuilder config = new ConnectionPoolConfigurationBuilder();
        config.setUsername("test");
        config.setPassword("test");
        config.setHost("localhost");
        config.setPort(5432);
        config.setDatabase("dbs1_imdb_3");

        /*
        Having the same maximum amount of connections as concurrent asynchronous requests seems to work
         */

        config.setMaxActiveConnections(100);
        connection = PostgreSQLConnectionBuilder.createConnectionPool(config);
    }
```

#### Necessary changes

1. Adjust your connection url
2. Adjust the connection configuration in the `MapActorsToAppearances` according to the `\conninfo` and `user&password` of your postgres role(user).

The builder configuration also offers many other settings. See the [jasync wiki page](https://github.com/jasync-sql/jasync-sql/wiki/Configuring-and-Managing-Connections) for more options.

### mapping incoming events

Flink will call the `asyncInvoke(Type INC, final CompletableFuture OUT)` function for each element that should be processed. To create a statement we first clear the actor names of any apostrophes in order to not escape our SQL string.

```
@Override
    public void asyncInvoke(String key, final ResultFuture<Tuple2<String, Integer>> resultFuture) throws Exception {

        /*
        asyncInvoke will be called for each incoming element
        the resultFuture is where the outgoing element will be
         */

        final String cleanedKey = key.replace("'", "''");
        final CompletableFuture<QueryResult> future = connection.sendPreparedStatement(
                "SELECT count(id) FROM movie INNER JOIN actor ON actor.movie_id = movie.mid WHERE actor.name LIKE '" + cleanedKey + "';");

        /*
        We create a new CompletableFuture which will be automatically and asynchronly done with the value
        from the given supplier.
         */
        CompletableFuture.supplyAsync(new Supplier<Long>() {
            @Override
            public Long get() {
                try {
                    QueryResult queryResult = future.get();
                    return queryResult.getRows().get(0).getLong(0);
                } catch (NullPointerException | InterruptedException | ExecutionException e) {
                    System.err.println(e.getMessage());
                    return null;
                }
            }

        }).thenAccept( (Long dbResult) -> {
            /*
            After the CompletableFuture is completed, the .thenAccept call will be made with the value from the CompletableFuture.
            We can use this value to set our return value into the function return (returnFuture).
             */
            resultFuture.complete(Collections.singleton(new Tuple2<>(cleanedKey, dbResult.intValue())));
        });
    }
```

`CompletableFuture.supplyAsync()` starts a new `Supplier` asynchronally whose `get()` method will try to return the query result. 

In jasync, a QueryResult contains a collection of rows (arrays). Each row can be retrieved by calling `.get(integer index)`, in our example we just retrieve the first row (0) since we only have one result row.

In each row, we can call `.getTYPE(integer index)` to get our cell back.
The type here needs to specify what kind of data jasync needs to read, see [their documentation](https://github.com/jasync-sql/jasync-sql/blob/master/db-async-common/src/main/java/com/github/jasync/sql/db/RowData.kt) for the types you can read.

When the result is returned, this CompletableFuture stage will finish, which is when `.thenAccept()` will start.

### Using the MapFunction

```
public class flinkWithJasync {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        //set the path
        final TextInputFormat format = new TextInputFormat(new Path("/home/vincent/Documents/WiSe1920/BP/cepta/src/main/java/org/bptlab/dbForFlink/all_actors.txt"));

        //set the path
        DataStream<String> actorNamesStream = streamExecutionEnvironment.readFile(format, "/home/vincent/Documents/WiSe1920/BP/cepta/src/main/java/org/bptlab/dbForFlink/all_actors.txt");

        /*
        This is the same as the following line:
            DataStream<String> actorNamesStream = streamExecutionEnvironment.readTextFile("/home/vincent/Documents/WiSe1920/BP/all_actors.txt");
        However, the readTextFile method is deprecated (but still works fine)
         */

        DataStream<Tuple2<String, Integer>> resultStream =
                AsyncDataStream.unorderedWait(actorNamesStream,
                        new MapActorNamesToAppearancesAsync(),
                        10000,
                        TimeUnit.MILLISECONDS,
                        5);

        resultStream.print();
        streamExecutionEnvironment.execute();
    }
}
```

**You will need to adjust the paths in `format` and `actorNamesStream`.**

Now we can create a new DataStream that will be created from an existing stream (our actorNames), our map function, the timeout length, its unit. The last parameter is for the amount of concurrent executions we want, in our case 5.

### Attention
You may need to set your capacity to 1 (or any number that suits your system). The capacity is the maximum number of asynchronous request. Two of our test systems handled 5 well, one system handled only one. 

**Be sure you have enough connections in a pool to run the concurrent executions!**

Running the example in your favourite IDE will produce a similar result:

```
2> (Darien, Frank,2)
3> (de Heer, Walter,1)
3> (De Keuchel, Dominique,1)
```



