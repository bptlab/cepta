package org.bptlab.cepta.serialization;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class AvroSerializer<T extends org.apache.avro.specific.SpecificRecordBase>
    implements org.apache.kafka.common.serialization.Serializer<T> {

  Schema schema;

  public <T extends org.apache.avro.specific.SpecificRecordBase> AvroSerializer(
      Supplier<? extends T> avroClass) {
    this.schema = avroClass.get().getSchema();
  }

  public AvroSerializer() {}

  @Override
  public void configure(Map<String, ?> map, boolean b) {}

  @Override
  public byte[] serialize(String s, T data) {
    try {
      DatumReader<Object> reader = new GenericDatumReader<>(schema);
      GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      Decoder decoder = DecoderFactory.get().jsonDecoder(schema, data.toString());
      Encoder encoder = EncoderFactory.get().binaryEncoder(output, null);
      Object datum = reader.read(null, decoder);
      writer.write(datum, encoder);
      encoder.flush();
      return output.toByteArray();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public void close() {}
}
