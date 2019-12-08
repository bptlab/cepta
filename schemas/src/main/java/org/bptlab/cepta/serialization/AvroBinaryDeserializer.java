package org.bptlab.cepta.serialization;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;

public abstract class AvroDeserializer<T extends org.apache.avro.specific.SpecificRecordBase>
    implements org.apache.kafka.common.serialization.Deserializer<T> {

  protected final Supplier<? extends T> targetType;

  public AvroDeserializer(Supplier<? extends T> targetType) {
    this.targetType = targetType;
  }

  @Override
  public void configure(Map<String, ?> map, boolean b) {}

  @SuppressWarnings("unchecked")
  @Override
  public T deserialize(String topic, byte[] data) {
    T result = null;
    try {
      if (data != null) {
        DatumReader<GenericRecord> datumReader =
            new SpecificDatumReader<>(targetType.get().getSchema());
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        result = (T) datumReader.read(null, decoder);
      }
    } catch (Exception e) {
      throw new SerializationException(
          "Can't deserialize data='" + Arrays.toString(data) + "' using generic avro deserializer");
    }
    return result;
  }

  @Override
  public void close() {}
}
