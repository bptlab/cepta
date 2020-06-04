# Operators and Patterns in and for Flink

## Operators

### CountOfTrainsAtStationFunction
Stream to Stream function  
**Implements:** ProcessWindowFunction<LiveTrainData, CountOfTrainsAtStationEvent, Long, TimeWindow>()
**Intput:** DataStream<LiveTrainData> inputStream  
**Output:** DataStream<CountOfTrainsInStationEvent>  
**Functionality:** generates CountOfTrainsInStationEvens in a timeWindow of 60 min sliding every 15 min

### DataCleansingFunction
Stream to Stream function
**Implements:** FilterFunction<T>()
**Intput:** DataStream<T> input Stream, T filter
**Parameter:** 
**Output:** DataStream<T> cleansedStream  
**Functionality:** Removes everything catched by the filter  

### DataToMongoDB<T extendsMessage> 
RichAsynchFunction<T, T>  
**Intput:** AsyncDataStream<T>
**Parameter:** String collectionName, MongoConfig mongoConfig
**Output:** processed DataStream  
**Functionality:** Saves every incoming event to the collection with the name collectionName in the database specified by the mongoConfig. This happens asynchronously. 

### DataToPostgresDataBase<T extendsMessage> 
MapFunction<T, T>  
**Intput:** DataStream<T>
**Parameter:** String collectionName, PostgresConfig postgresConfig
**Output:** processed DataStream  
**Functionality:** Saves every incoming event to the collection with the name collectionName in the database specified by the postgresConfig. The Upload happens asynchronously. **But for each Element the connection pool will be established!**

### DelayShiftFunction<LiveTrainData> 
RichAsynchFunction<LiveTrainData, Notification>  
**Intput:** AsyncDataStream<LiveTrainData>
**Parameter:** PostgresConfig postgresConfig, (Optional) Long  DelayThreshold (Default is abs(60sec)) 
**Output:** DataStream<Notification>  
**Functionality:** Generates DelayNotifications for all subsequent stations of the incoming event with the same delay. The planned Events are fetched from a mongodb. 

### DelayShiftFunctionMongo<LiveTrainData> 
RichAsynchFunction<LiveTrainData, Notification>  
**Intput:** AsyncDataStream<LiveTrainData>
**Parameter:** MongoConfig mongoConfig, (Optional) Long  DelayThreshold (Default is abs(60sec))
**Output:** DataStream<Notification>  
**Functionality:** Generates DelayNotifications for all subsequent stations of the incoming event with the same delay. The planned Events are fetched from a mongodb.

### DetectStationArrivalDelay
ProcessFunction<Tuple2<LiveTrainData, PlannedTrainData>, NotificationOuterClass.Notification>  
**Intput:** DataStream<Tuple2<LiveTrainData, PlannedTrainData>> inputStream
**Parameter:** none
**Output:** DataStream<Notification>   
**Functionality:** Compares dates of live and plan dataset. If there is a delay it generates a delay. Threshold is abs(10sec)
  
### EnrichDelayWithCoordinatesFunction
RichFlatMapFunction<NotificationOuterClass.Notification, NotificationOuterClass.Notification>
**Input:** DataStream<Notification>
**Output:** DataStream<Notification>
**Functionality:** This Function takes an inputstream of DelayNotifications and enriches the events with information about the coordinations of the dedicated station

### LivePlannedCorrelationFunction
RichAsynchFunction<LiveTrainData, Tuple2<LiveTrainData, PlannedTrainData>>  
**Intput:** AsyncDataStream<LiveTrainData>
**Parameter:** PostgresConfig postgresConfig
**Output:** DataStream<Tuple2<LiveTrainData, PlannedTrainData>>  
**Functionality:** fetches matching planned train event for each incoming live train event. The planned events are fetched with the information of the postgresConfig.

### RemoveDuplicatesFunction
Stream to Stream function
**Implements:** ProcessWindowFunction<T, T, Integer, GlobalWindow>
**Intput:** DataStream<T> inputStream, 
**Parameter:** int windowSize
**Output:** DataStream<T> duplicateFreeStream  
**Functionality:** Removes duplicates in every event count window of the windowSize. Its a tumbling window.

### SumOfDelayAtStationFunction
Stream to Stream function
**Implements:** ProcessWindowFunction<NotificationOuterClass.Notification, Tuple2<Long, Double>, Integer, GlobalWindow>()
**Intput:** DataStream<DelayNotification> inputStream
**Parameter:** int windowSize
**Output:** DataStream<Tuple2<Long, Double>>  
**Functionality:** sums up all delay based on the location Id's in the given window size. The window is a fixed event number window.

### WeatherLiveTrainJoinFunction
Stream to Stream function
**Implements:** RichJoinFunction<Tuple2<WeatherData, Integer>, LiveTrainData, NotificationOuterClass.Notification>()
**Intput:** DataStream<Tuple2<WeatherData, Integer>> weather, DataStream<LiveTrainData> train
**Parameter:** none
**Output:** DataStream<Notification>  
**Functionality:** matches live train and weather data (enriched with stationId) to generate DelayNotifications according to the weather condition.

### WeatherLocationCorrelationFunction
RichAsynchFunction<WeatherData, Tuple2<WeatherData, Integer>>  
**Intput:** AsyncDataStream<WeatherData>
**Parameter:** PostgresConfig postgresConfig 
**Output:** Tuple2<WeatherData, Integer>  
**Functionality:** Assigns a stationId to every incoming weather event. The stations ar fetched from the database specified in the postgresConfig

## Patterns

### StaysInStationPattern
**Stream:** LiveTrainData  
**Matches:** status=3 is followed by a status=4  
**Meaning:** Train stays in station for a while  
**ProcessFunction:** creates StaystInStationEvent 

### NoMatchingPlannedTrainPattern
**Stream:** Tuple2<LiveTrainData, PLannedTrainData> (see [LivePlannedCorrelationFunction](https://github.com/bptlab/cepta/blob/dev/docs/flink/operators_patterns.md#liveplannedcorrelationfunction))  
**Matches:** tuple.f1 == null  
**Meaning:**  a LiveTrainDataEvent does not have a corresponding PlannedTrainData  
**ProcessFunction:** creates StaystInStationEvent 
