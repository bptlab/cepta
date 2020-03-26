package org.bptlab.cepta.operators;

import java.lang.Object;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;

/*This class implements the cleanseStream function which cleanses a DataSteam from one specific value.
All inputs must implement the .equals method*/
public class DataCleansingFunction <T extends Object> {
    
  public DataStream<T> cleanseStream(DataStream<T> inputStream, T filter) {
    DataStream<T> resultStream = inputStream.filter(new FilterFunction<T>() {
        @Override
        public boolean filter(T value) throws Exception {
          return (!(value.equals(filter)));
        }
      });
    return resultStream;
  }
}