package org.bptlab.cepta.providers;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import com.google.protobuf.Timestamp;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.providers.ReplayerProvider;
import org.bptlab.cepta.containers.ReplayerContainer;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.QueryOptions;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.SourceQuery;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.SourceQueryOptions;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.Timerange;
import org.bptlab.cepta.models.constants.topic.TopicOuterClass.Topic;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.ReplayedEvent;

/*
This provider is supposed to replay one specific trainId from our DB datasets (RedBull example).
The trainId for the following example is: trainID 49054 on 2019-09-05 but this can be changed in no time.
*/

public class RedBullExampleProvider {

    // RedBull example
    public static int trainID = 49054;
    public static String dateString = "2019-09-05";

    // Build query for the Replayer
    public static QueryOptions getQueryOptions(){
      // this represents the timestamp 2019-09-05 00:00:00.0
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      String dateInString = dateString;
      long epochMillis = 0l;

      try{
        Date date = sdf.parse(dateInString);
        epochMillis = date.getTime();
      } catch (ParseException e) {
        // System.out.println(e)
      }

      Timestamp timestampStart = Timestamp.newBuilder().setSeconds((int)(epochMillis / 1000))
          .setNanos((int) ((epochMillis % 1000) * 1000000)).build();
      // Wait 1 week till end (604800 seconds = 60 * 60 * 24 * 7)
      Timestamp timestampEnd = Timestamp.newBuilder().setSeconds((int)(epochMillis / 1000) + 604800)
          .setNanos((int) ((epochMillis % 1000) * 1000000)).build();

      Timerange.Builder timerangeBuilder = Timerange.newBuilder();
      timerangeBuilder.setStart(timestampStart);
      timerangeBuilder.setEnd(timestampEnd);

      SourceQueryOptions.Builder sourceQueryOptionsBuilder = SourceQueryOptions.newBuilder();
      sourceQueryOptionsBuilder.setTimerange(timerangeBuilder.build());
      sourceQueryOptionsBuilder.setLimit(1);
      sourceQueryOptionsBuilder.setOffset(1);

      SourceQuery.Builder sourceQueryBuilder = SourceQuery.newBuilder();
      sourceQueryBuilder.setSource(Topic.CHECKPOINT_DATA);
      sourceQueryBuilder.addIds(Integer.toString(trainID));
      sourceQueryBuilder.setOptions(sourceQueryOptionsBuilder.build());

      QueryOptions.Builder optionsBuilder = QueryOptions.newBuilder();
      optionsBuilder.addSources(sourceQueryBuilder.build());
      optionsBuilder.setSort(true);

      return optionsBuilder.build();
    }

    public static DataStream<ReplayedEvent> RedBullExampleCheckpointDataStream(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);

      ReplayerProvider dataProvider = new ReplayerProvider(new ReplayerContainer());
      DataStream<ReplayedEvent> redbullExampleStream = env.fromCollection(dataProvider.query(RedBullExampleProvider.getQueryOptions()));

      return redbullExampleStream;
    }
}
