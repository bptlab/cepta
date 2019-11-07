package connectors.consumer_producer;

import java.util.Map;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;


public class TrainDataDeserializer implements Deserializer<TrainData> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public TrainData deserialize(String topic, byte[] data) {

        ObjectMapper mapper = new ObjectMapper();

        TrainData object = null;

        try {

            object = mapper.readValue(data, TrainData.class);

        } catch (Exception exception) {

            System.out.println("Error in deserializing bytes "+ exception);

        }

        return object;

    }

    @Override
    public void close() {

    }


}
