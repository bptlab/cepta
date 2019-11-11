package connectors.consumer_producer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.kafka.common.serialization.Serializer;
import org.codehaus.jackson.map.ObjectMapper;

public class TrainDataSerializer implements Serializer<TrainData> {
    // serializes TrainData

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String arg0, TrainData data) {
/*
        byte[] retVal = null;
        BinaryEncoder encoder = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            retVal = objectMapper.writeValueAsString(data).getBytes();
        } catch (Exception exception) {
            System.out.println("Error in serializing object"+ data);
        }

        return retVal;
*/
        try {
            Schema schema = new Schema.Parser().parse(new File("/src/main/avro/trainData.avsc"));
            DatumReader<Object> reader = new GenericDatumReader<>(schema);
            GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Decoder decoder = DecoderFactory.get().jsonDecoder(schema, data.toString());
            Encoder encoder = EncoderFactory.get().binaryEncoder(output, null);
            Object datum = reader.read(null, decoder);
            writer.write(datum, encoder);
            encoder.flush();
            return output.toByteArray();
        } catch (IOException e){
        }
    return null;
    }

    @Override
    public void close() {
    }


}
