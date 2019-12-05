package org.bptlab.cepta.serialization;

import java.io.IOException;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.kafka.common.serialization.Serializer;
import org.bptlab.cepta.LiveTrainData;

public class TrainDataRunningSerializer implements Serializer<LiveTrainData> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {}

  @Override
  public byte[] serialize(String arg0, LiveTrainData data) {
    try {
      Schema schema = LiveTrainData.SCHEMA$;
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
    }
    return null;
  }

  @Override
  public void close() {}
}
