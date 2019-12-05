package org.bptlab.cepta.serialization;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.kafka.common.serialization.Deserializer;
import org.bptlab.cepta.LiveTrainData;

public class TrainDataRunningDeserializer implements Deserializer<LiveTrainData> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {}

  @Override
  public LiveTrainData deserialize(String topic, byte[] data) {
    try {
      boolean pretty = false;
      Schema schema = LiveTrainData.SCHEMA$;
      GenericDatumReader<Object> reader = new GenericDatumReader<>(schema);
      DatumWriter<Object> writer = new GenericDatumWriter<>(schema);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, output, pretty);
      Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
      Object datum = reader.read(null, decoder);
      writer.write(datum, encoder);
      encoder.flush();
      output.flush();
      String stringData = new String(output.toByteArray(), StandardCharsets.UTF_8);
      Gson gson = new Gson();
      LiveTrainData trainDataRunning = gson.fromJson(stringData, LiveTrainData.class);
      return trainDataRunning;
    } catch (IOException e) {

    }

    return null;
  }

  @Override
  public void close() {}
}
