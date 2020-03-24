package org.bptlab.cepta.operators;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;

/*This class implements the cleanseStream function which cleanses a DataSteam from one specific value.
Currently supported are the following DataTypes:
-Long
-Float
-Integer
-String
-Boolean
*/
public class DataCleansingFunction {
    
    public static DataStream<Long> cleanseStream(DataStream<Long> InputStream, Long FilterLong) {
        DataStream<Long> ResultStream = InputStream.filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long value) throws Exception {
              return (!(value.equals(FilterLong)));
            }
          });
        return ResultStream;
    }

    public static DataStream<Double> cleanseStream(DataStream<Double> InputStream, Double FilterDouble) {
      DataStream<Double> ResultStream = InputStream.filter(new FilterFunction<Double>() {
          @Override
          public boolean filter(Double value) throws Exception {
            return (!(value.equals(FilterDouble)));
          }
        });
      return ResultStream;
  }
    
    public static DataStream<Integer> cleanseStream(DataStream<Integer> InputStream, Integer FilterInteger) {
        DataStream<Integer> ResultStream = InputStream.filter(new FilterFunction<Integer>() {
            @Override
            public boolean filter(Integer value) throws Exception {
              return (!(value.equals(FilterInteger)));
            }
          });
        return ResultStream;
    }
   
    public static DataStream<String> cleanseStream(DataStream<String> InputStream, String FilterString) {
        DataStream<String> ResultStream = InputStream.filter(new FilterFunction<String>() {
            @Override
            public boolean filter(String value) throws Exception {
              return (!(value.equals(FilterString)));
            }
          });
        return ResultStream;
    }

    public static DataStream<Boolean> cleanseStream(DataStream<Boolean> InputStream, Boolean FilterBoolean) {
        DataStream<Boolean> ResultStream = InputStream.filter(new FilterFunction<Boolean>() {
            @Override
            public boolean filter(Boolean value) throws Exception {
              return (!(value.equals(FilterBoolean)));
            }
          });
        return ResultStream;
    }
}