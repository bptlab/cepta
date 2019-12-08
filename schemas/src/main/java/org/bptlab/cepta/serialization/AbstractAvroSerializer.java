package org.bptlab.cepta.serialization;

import java.io.IOException;
import java.util.Map;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.io.ParsingEncoder;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.kafka.common.errors.SerializationException;

public abstract class AbstractAvroSerializer<T extends org.apache.avro.specific.SpecificRecordBase>
    implements org.apache.kafka.common.serialization.Serializer<T> {

  public AbstractAvroSerializer() {}

  public abstract Encoder getEncoder(T data, ByteArrayOutputStream outputStream) throws IOException;

  @Override
  public void configure(Map<String, ?> map, boolean b) {}

  @Override
  public byte[] serialize(String s, T data) {
    byte[] result = null;
    try {
      if (data != null) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = getEncoder(data, outputStream);
        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(data.getSchema());
        writer.write(data, encoder);
        encoder.flush();
        outputStream.close();
        result = outputStream.toByteArray();
      }
    } catch (IOException e) {
      throw new SerializationException(
          "Can't serialize data='" + data + "' using generic avro serializer");
    }
    return result;
  }

  @Override
  public void close() {}
}
