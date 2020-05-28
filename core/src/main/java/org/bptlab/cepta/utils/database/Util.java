package org.bptlab.cepta.utils.database;

import org.apache.commons.text.CaseUtils;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.ArrayList;
import java.util.Map;

public class Util {
    
  public static ProtoInfoStrings getInfosOfProtoMessageAsStrings(Message dataSet) throws NoSuchFieldException, IllegalAccessException {
    ArrayList<String> columnNames = new ArrayList<String>();
    ArrayList<String> values = new ArrayList<>();
    ArrayList<String> types = new ArrayList<>();
    for (Map.Entry<FieldDescriptor,java.lang.Object> entry : dataSet.getAllFields().entrySet()) {

      columnNames.add(  CaseUtils.toCamelCase(entry.getKey().getName(),false,'_'));

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
    ProtoInfoStrings protoInfo = new ProtoInfoStrings(columnNames, values, types);
    return protoInfo;
  }
      
  public static ProtoKeyValues getKeyValuesOfProtoMessage(Message dataSet) throws NoSuchFieldException, IllegalAccessException {
    ArrayList<String> columnNames = new ArrayList<String>();
    ArrayList<Object> values = new ArrayList<Object>();
    for (Map.Entry<FieldDescriptor,java.lang.Object> entry : dataSet.getAllFields().entrySet()) {
      columnNames.add(  CaseUtils.toCamelCase(entry.getKey().getName(),false,'_'));
      values.add(entry.getValue());
    }
    ProtoKeyValues protoInfo = new ProtoKeyValues(columnNames,values);
    return protoInfo;
  }

  public static java.sql.Timestamp ProtoTimestampToSqlTimestamp(com.google.protobuf.Timestamp protoTimestamp){
    long seconds = protoTimestamp.getSeconds();
    java.sql.Timestamp timestamp = new java.sql.Timestamp(seconds*1000);
    return timestamp;
  }

  public static class ProtoInfoStrings{
    ArrayList<String> columnNames;
    ArrayList<String> values;
    ArrayList<String> types;

    public ProtoInfoStrings(){
    }

    public ProtoInfoStrings(ArrayList<String> columnNames, ArrayList<String> values, ArrayList<String> types){
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

  public static class ProtoKeyValues{
    ArrayList<String> columnNames;
    ArrayList<Object> values;

    public ProtoKeyValues(){
    }

    public ProtoKeyValues(ArrayList<String> columnNames, ArrayList<Object> values){
        this.columnNames = columnNames;
        this.values = values;
    }

    public ArrayList<String> getColumnNames(){
      return this.columnNames;
    }
    public ArrayList<Object> getValues(){
      return this.values;
    }
  }
}