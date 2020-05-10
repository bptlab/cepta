package org.bptlab.cepta.serialization;

import com.google.protobuf.Message;
import com.twitter.chill.protobuf.ProtobufSerializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkProtoSerializer {

    public static void registerTypes(StreamExecutionEnvironment env, Message... classes) {
        for (Message msg : classes) {
            env.getConfig().registerTypeWithKryoSerializer(msg.getClass(), ProtobufSerializer.class);
        }
    }
}
