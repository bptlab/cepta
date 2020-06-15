package org.bptlab.cepta.utils.monitor;

import org.apache.kafka.common.serialization.StringSerializer;
import org.bptlab.cepta.config.KafkaConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.*;

public class CepMonitor {
    public static void initializeMonitoring(Map<String,?> toMonitorStreams) {
        Properties props = new Properties();
        props.put("bootstrap.servers", new KafkaConfig().getBroker()/*"localhost:9092"*/);
        props.put("group.id", "MonitorInitializer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        AdminClient adminClient = AdminClient.create(props);
        List<NewTopic> newTopics = new ArrayList<NewTopic>();
        for (Map.Entry<String, ?> stream : toMonitorStreams.entrySet() ) {
            NewTopic newTopic = new NewTopic(stream.getKey(), 1, (short) 1); //new NewTopic(topicName, numPartitions, replicationFactor)

            newTopics.add(newTopic);
        }
        CreateTopicsResult result = adminClient.createTopics(newTopics);
        System.out.print(result);
        adminClient.close();
    }
}



