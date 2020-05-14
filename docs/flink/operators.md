# Operators in and for Flink

## Functions
### AverageSpeedFunction
not implemented

### DataCleansingFunction
Stream to Stream function  
**Intput:** DataStream<T> input Stream, T filter  
**Output:** DataStream<T> cleansedStream  
**Functionality:** Removes everything catched by the filter  

### DataToMongoDB<T extendsMessage> 
RichAsynchFunction<T, T>  
**Intput:** String collectionName, MongoConfig mongoConfig  
**Output:** processed DataStream  
**Functionality:** Saves every incoming event to the collection with the name collectionName in the database specified by the mongoConfig.  
This happens asynchronously. 

### DataToPostgresDB<T extendsMessage> 
RichAsynchFunction<T, T>  
**Intput:** String collectionName, PostgresConfig postgresConfig  
**Output:** processed DataStream  
**Functionality:** Saves every incoming event to the collection with the name collectionName in the database specified by the postgresConfig.  
This happens asynchronously. 

### DelayShiftFunction<LiveTrainData> 
RichAsynchFunction<LiveTrainData, Notification>  
**Intput:** PostgresConfig postgresConfig  
**Output:** DataStream<Notification>  
**Functionality:** Generates DelayNotifications for all subsequent stations of the incoming event with the same delay. The planned Events are fetched from a mongodb.

### DelayShiftFunctionMongo<LiveTrainData> 
RichAsynchFunction<LiveTrainData, Notification>  
**Intput:** MongoConfig mongoConfig  
**Output:** DataStream<Notification>  
**Functionality:** Generates DelayNotifications for all subsequent stations of the incoming event with the same delay. The planned Events are fetched from a mongodb.

### DetectStationArrivalDelay
Stream to Stream function  
**Intput:** DataStream<Tuple2<LiveTrainData, PlannedTrainData>> inputStream
**Output:** DataStream<Notification>  
**Functionality:** Compares dates of live and plan dataset. If there is a delay it generates a delay.

### GenerateIdFunction 
not implemented

## Patterns
