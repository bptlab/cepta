package org.bptlab.cepta.serialization;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class AvroBinaryFlinkSerializationSchema<T extends SpecificRecordBase> implements
    SerializationSchema<T> {
  @Override
  public byte[] serialize(T o) {
    return new AvroBinarySerializer<T>().serialize("", o);
  }
}