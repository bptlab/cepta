package org.bptlab.cepta.serialization;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class AvroDeserializer<T extends org.apache.avro.specific.SpecificRecordBase>
    implements org.apache.kafka.common.serialization.Deserializer<T> {

  Schema schema;

  public AvroDeserializer(Supplier<? extends T> avroClass) {
    this.schema = avroClass.get().getSchema();
  }

  public AvroDeserializer() {}

  @Override
  public void configure(Map<String, ?> map, boolean b) {}

  @Override
  public T deserialize(String s, byte[] data) {
    try {
      GenericDatumReader<Object> reader = new GenericDatumReader<>(schema);
      DatumWriter<Object> writer = new GenericDatumWriter<>(schema);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, output, false);
      Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
      Object datum = reader.read(null, decoder);
      writer.write(datum, encoder);
      encoder.flush();
      output.flush();
      String stringData = new String(output.toByteArray(), StandardCharsets.UTF_8);
      Gson gson = new Gson();
      return gson.fromJson(stringData, new TypeToken<T>() {}.getType());
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public void close() {}
}
