package org.bptlab.cepta.utils;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
// import com.google.protobuf.Timestamp;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import org.javatuples.Triplet;

public class Util {
    
  public static ProtoInfo getInfosOfProtoMessage(Message dataSet) throws NoSuchFieldException, IllegalAccessException {
    ArrayList<String> columnNames = new ArrayList<String>();
    ArrayList<String> values = new ArrayList<String>();
    ArrayList<String> types = new ArrayList<>();
    for (Map.Entry<FieldDescriptor,java.lang.Object> entry : dataSet.getAllFields().entrySet()) {
      System.out.println(entry.getKey() + "/" + entry.getValue());

      columnNames.add(entry.getKey().getName());

      if(entry.getValue() instanceof com.google.protobuf.Timestamp){
        values.add(String.format("'%s'", ProtoTimestampToSqlTimestamp((com.google.protobuf.Timestamp)entry.getValue()).toString()));
        types.add("timestamp");
      }
      else if(entry.getValue() instanceof String){
        // add ' ' around value if it's a string
        values.add(String.format("'%s'", entry.getValue().toString()));
        types.add("varchar");
      }else{
        values.add(entry.getValue().toString());
        if (entry.getValue() instanceof Long){
          types.add("bigint");
        }else if(entry.getValue() instanceof Double){
          types.add("float8");
        }
      }
    }
    ProtoInfo protoInfo = new ProtoInfo(columnNames,values,types);
    return protoInfo;
  }

  public static java.sql.Timestamp ProtoTimestampToSqlTimestamp(com.google.protobuf.Timestamp protoTimestamp){
    long seconds = protoTimestamp.getSeconds();
    java.sql.Timestamp timestamp = new java.sql.Timestamp(seconds*1000);
    return timestamp;
  }

  public static class ProtoInfo{
      ArrayList<String> columnNames;
      ArrayList<String> values;
      ArrayList<String> types;

      public ProtoInfo(){
      }

      public ProtoInfo(ArrayList<String> columnNames, ArrayList<String> values, ArrayList<String> types){
          this.columnNames = columnNames;
          this.values = values;
          this.types = types;
      }

      public ArrayList<String> getColumnNames(){
        return this.columnNames;
      }
      public ArrayList<String> getValues(){
        return this.values;
      }
      public ArrayList<String> getTypes(){
        return this.types;
      }
  }
}