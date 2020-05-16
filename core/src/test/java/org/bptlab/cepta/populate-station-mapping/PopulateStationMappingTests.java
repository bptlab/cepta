package org.bptlab.cepta;

import org.bptlab.cepta.config.PostgresConfig;

public class PopulateStationMappingTests {

  private static final boolean debug = false;

  /*@Test
  public void testPopulateStationMappings() throws IOException, InterruptedException {
      KafkaContainer kafka = new KafkaContainer();
      kafka.start();
      ReplayerContainer replayer = ReplayerContainer.forKafka(kafka);
      replayer.start();

      // Start the replayer for the test
      ReplayOptions replayOptions = ReplayOptions.newBuilder().setLimit(100).build();
      SourceQueryOptions gps = SourceQueryOptions.newBuilder().setSource(Topic.GPS_TRIP_UPDATE_DATA).setOptions(replayOptions).build();
      QueryOptions queryOptions = QueryOptions.newBuilder().addAllSources(Arrays.asList(gps)).build();
      Iterator<ReplayedEvent> eventStream = new ReplayerProvider(replayer).query(queryOptions);

      final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

      eventStream.forEachRemaining(e -> System.out.println(e));
  }*/
}
