package connectors.consumer_producer;

import com.google.gson.Gson;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class TrainDataDeserializer implements Deserializer<WeatherData> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public WeatherData deserialize(String topic, byte[] data) {
        try {
            boolean pretty = false;
            Schema schema = WeatherData.SCHEMA$;
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
            WeatherData weatherData = gson.fromJson(stringData, WeatherData.class);
            return weatherData;
        } catch (IOException e) {

        }

        return null;
    }

    @Override
    public void close() {

    }


}
