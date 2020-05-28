package org.bptlab.cepta.operators;

import java.lang.Object;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import java.lang.reflect.*;

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

  public DataStream<T> cleanseStream(DataStream<T> inputStream, Integer filterValue, String filterAttribute) {
  
    String methodName = "get" + filterAttribute;
    
    DataStream<T> resultStream = inputStream.filter(new FilterFunction<T>() {
      
        @Override
        public boolean filter(T element) throws Exception {
          Class c = element.getClass();
          Method method = c.getDeclaredMethod(methodName);
          String value = method.invoke(element).toString();
          return (!(value.equals(filterValue.toString())));
        }
      });
    return resultStream;
  }

}