package org.bptlab.cepta.serialization;

import com.google.protobuf.GeneratedMessage;
import org.apache.flink.api.common.serialization.SerializationSchema;


public class BinaryProtoFlinkSerializationSchema<T extends GeneratedMessage> implements
    SerializationSchema<T> {

    Class<T> targetType;

  public BinaryProtoFlinkSerializationSchema(Class<T> targetType) {
    this.targetType = targetType;
  }

    @Override
  public byte[] serialize(T value) {
      return value.toByteArray();
  }
}