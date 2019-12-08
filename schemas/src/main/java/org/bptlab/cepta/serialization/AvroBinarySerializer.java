package org.bptlab.cepta.serialization;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroSerializer<T extends org.apache.avro.specific.SpecificRecordBase>
    implements org.apache.kafka.common.serialization.Serializer<T> {

  private static final Logger logger =
      LoggerFactory.getLogger(AvroSerializer.class.getName());

  Schema schema;

  public <T extends org.apache.avro.specific.SpecificRecordBase> AvroSerializer(
      Supplier<? extends T> avroClass) {
    this.schema = avroClass.get().getSchema();
  }

  public AvroSerializer(Schema schema) {
    this.schema = schema;
  }

  // public static AvroSerializer forEvent(Supplier<? extends org.apache.avro.specific.SpecificRecordBase> avroSchemaClass) {
  public static AvroSerializer forEvent(org.apache.avro.specific.SpecificRecordBase avroSchemaClass) {
    return new AvroSerializer(avroSchemaClass.getSchema());
  }

  public AvroSerializer() {}

  @Override
  public void configure(Map<String, ?> map, boolean b) {}

  @Override
  public byte[] serialize(String s, T data) {
    byte[] result = null;
    try {
      // JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, data.toString());
      if (data != null) {
        logger.error(data.getSchema().toString());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder =
            EncoderFactory.get().binaryEncoder(outputStream, null);
        // JsonEncoder encoder = EncoderFactory.get().jsonEncoder(output, null);
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

    /*
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
    }*/
  }

  @Override
  public void close() {}
}
