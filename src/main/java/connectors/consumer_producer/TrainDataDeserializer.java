package connectors.consumer_producer;

import java.io.IOException;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.kafka.common.serialization.Deserializer;
import com.google.gson.Gson;


public class TrainDataDeserializer implements Deserializer<TrainData> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public TrainData deserialize(String topic, byte[] data) {
        try {
            boolean pretty = false;
            Schema schema = TrainData.SCHEMA$;
            GenericDatumReader<Object> reader = new GenericDatumReader<>(schema);
            DatumWriter<Object> writer = new GenericDatumWriter<>(schema);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, output, pretty);
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            Object datum = reader.read(null, decoder);
            writer.write(datum, encoder);
            encoder.flush();
            output.flush();
            String stringData = new String(output.toByteArray(), "UTF-8");
            Gson gson = new Gson();
            TrainData trainData = gson.fromJson(stringData, TrainData.class);
            return trainData;
        } catch (IOException e){

        }

        return null;
    }

    @Override
    public void close() {

    }


}
