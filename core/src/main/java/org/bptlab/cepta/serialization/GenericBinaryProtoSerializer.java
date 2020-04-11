package org.bptlab.cepta.serialization;

import org.apache.flink.api.common.serialization.SerializationSchema;
import com.google.protobuf.GeneratedMessageV3;

public class GenericBinaryProtoSerializer<T extends GeneratedMessageV3> implements SerializationSchema<T> {

  @Override
    public byte[] serialize(T value) {
        return value.toByteArray();
    }
}