package org.bptlab.cepta.serialization;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Parser;
import java.io.IOException;
import java.lang.Exception;

public class GenericBinaryProtoDeserializer<T extends GeneratedMessageV3> implements DeserializationSchema<T> {

    private T defaultInstance;
    private TypeInformation<T> typeInfo;
    
    // Transient types we only use for caching
    private transient Parser<T> parser;

  public GenericBinaryProtoDeserializer(Class<T> targetType) {
    this.typeInfo = TypeInformation.of(targetType);
    try {
      this.defaultInstance = (T) targetType.getMethod("getDefaultInstance").invoke(null);
    } catch (Exception e) {
      System.out.println(e.toString());
      System.out.println("Failed to get parser");
    }
  }

    private Parser<T> getParser() {
        if (this.parser == null) {
          this.parser = (Parser<T>) this.defaultInstance.getParserForType();
        }
        return this.parser;
    }

    @Override
    public T deserialize(byte[] message) throws IOException {
        try {
            Parser<T> parser = this.getParser();
            if (parser == null) {
                throw new IOException("No parser for given message");
            }

            return parser.parseFrom(message);
        } catch (Exception e) {
            System.out.println(e.toString());
            throw new IOException("Unable to deserialize bytes");
        }
    }

    @Override
    public boolean isEndOfStream(GeneratedMessageV3 nextElement) {
        // This can be overwritten when testing to end the stream when a last element is received
        return false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return this.typeInfo;
    }
}