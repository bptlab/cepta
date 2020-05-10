package org.bptlab.cepta.serialization;

import com.google.protobuf.Message;

import java.lang.reflect.Method;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Kryo serializer for protobouf classes.
 */
public class KryoProtoSerializer extends Serializer<Message> {

    private static final int PROTO_CLASS_CACHE_SIZE = 2_000;

    private static LoadingCache<Class<Message>, Method> protoParseMethodsCache =
            CacheBuilder.newBuilder()
                    .maximumSize(PROTO_CLASS_CACHE_SIZE)
                    .build(
                            new CacheLoader<Class<Message>, Method>() {
                                public Method load(Class<Message> protoClass) {
                                    try {
                                        return protoClass.getMethod("parseFrom", new Class[]{byte[].class});
                                    } catch (NoSuchMethodException e) {
                                        throw new RuntimeException(
                                                "Couldn't get parseFrom method for proto class: " + protoClass);
                                    }
                                }
                            });

    @Override
    public void write(Kryo kryo, Output output, Message protoObj) {
        byte[] bytes = protoObj.toByteArray();
        output.writeInt(bytes.length, true);
        output.writeBytes(bytes);
    }

    @Override
    public Message read(Kryo kryo, Input input, Class<Message> protoClass) {
        try {
            int size = input.readInt(true);
            byte[] bytes = new byte[size];
            input.readBytes(bytes);
            return (Message) protoParseMethodsCache.get(protoClass).invoke(null, bytes);
        } catch (Exception e) {
            throw new RuntimeException("Could not get parse method for: " + protoClass, e);
        }
    }
}
