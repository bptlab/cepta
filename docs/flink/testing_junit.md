# Introduction
In the following we want to give a short overview on how we want to use [JUnit](https://junit.org/junit4/) to test our implementations in Flink.

# How to test on DataStreams

## Set up Environment / Mock Data
To test operations on data streams we need some data we can test with. We decided to create data provider classes with methods to provide different data streams.

As always in Flink we need an ExecutionEnvironment to create our DataStreams. After this, we can set up our data set.

```java
public class WindSpeedDataProvider {
  public static DataStream<Integer> dataProviderMethod()
  {
    StreamExecutionEnvironment env;
    env = StreamExecutionEnvironment.createLocalEnvironment();
    // set Parallelism to 1 so make sure the test data is transfered in the right order
    env.setParallelism(1);
    DataStream<Integer> windspeed = env.fromElements(5,4,4);
    return windspeed;
  }
}
```
Parallelism has to be set to 1 because you can not be sure about the order of the streamed elements otherwise.
With parallelism set to 1, you can be sure your events will get to your function in the order you declared in your data stream. If parallelism is larger than one, the events are distributed at a non-deterministic order and you can't be sure which event will occur first. 

## Create a DataStream
To create a data stream with elements of a given _Type_ you can use the following method.
```java
DataStream<Type> stream = env.fromElements(el1, el2, el3,...);
```
See more details and other methods like _fromElements()_ or _fromCollection()_ at https://ci.apache.org/projects/flink/flink-docs-stable/api/java/org/apache/flink/streaming/api/environment/StreamExecutionEnvironment.html#fromElements-OUT...-

## Use the data in your test

With your DataProvider ready to go, you only need to call its functions to fetch the data. 

```java
public class DetectAgentTests {
  // tests our custom filter agent, which filters all integers larger than 3
  public void testStormFilter() throws IOException {
  DataStream<integer> windspeed = WindSpeedDataProvider.dataProviderMethod();
  [...]
}
```

After the @Test annotation, you specify the name of the provider and the DataProvider class. 
You can now give your test a parameter, which represents the data the DataProvider sends. 

## Operate on the stream
Now you can operate away on the DataStream with whatever you want to test. If you want you can store it in an extra stream. Otherwise, continue with the next step and use the resulting stream of your operations.

## Get data from stream to collection
To test whether our Stream has what we want, its elements will be collected. This happens through putting everything in an Iterator so we can iterate over the elements later and put them into an _ArrayList_ (or other _Collection_).
```java
ArrayList<Integer> testOutputList = new ArrayList<>();

Iterator<Integer> testOutputIterator = DataStreamUtils.collect(StreamingJob.stormFilter(windspeed));

while (testOutputIterator.hasNext()) {
  testOutputList.add(testOutputIterator.next());
}
```
## Compare with expected output
To compare your test output with the expected result put the expected elements in a _Collection_ and compare it with the collected one.
```java
Assert.assertTrue(CollectionUtils.isEqualCollection(expectedCollection, testOutputList));
```
_CollectionUtils.isEqualCollection()_ considers the elements and their cardinality in the collections.

## Attention
- Sometimes you need to iterate over a stream to make flink understand it has to actually give the events of a 
stream. Otherwise it can happen that your functions appear to operate on empty streams.
- Collecting a stream strips it of its ability to provide the events, so you should iterate only after doing all that you wanted to do with the stream.
- `dataStream.print()` can print a stream. This is an operation that has to be done before collecting the stream with an iterator

# Conventions
## What to test
### Don't test:
- Flink Api
- Kafka
- the Database
- JDBC (the database connector)

But make sure it works nonetheless. For example by testing it manually.

You will probably notice when some of this does not work.

### Do test:
- your own methods implemented in Flink/Java
- Patterns, Filter, ...you wrote/configured yourself

## Components
Put tests in classes depending on what they test (e.g. DetectAgentTests). The class should consist of a noun followed by "Tests".

Test methods should be named "test" followed by what it tests. E.g. "testStormFilter".

