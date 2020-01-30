package org.bptlab.cepta.serialization;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.types.DeserializationException;

public class BinaryProtoFlinkDeserializationSchema<T extends GeneratedMessage> implements
    DeserializationSchema<T> {

    // Supplier<? extends T> targetType;
    // Supplier<? extends T> targetType;
    Class<T> targetType;

  // protected final Class<T> targetType;
  // public BinaryFlinkDeserializationSchema(Supplier<? extends T> targetType) {
  public BinaryProtoFlinkDeserializationSchema(Class<T> targetType) {
    this.targetType = targetType;
  }

    @Override
  public T deserialize(byte[] message) {
      T result = null; // (Message)
      try {
          // return Parser<T>()->parseFrom(message);
          // result = (Message) targetType.getDeclaredConstructor().newInstance().parseFrom(message);  //.getParserForType()
        result = (T) targetType.getDeclaredConstructor().newInstance().getParserForType().parseFrom(message);  //.getParserForType()
        // return T.parser().parseWithIOException(message); //.getParserForType().parseFrom(message);
      } catch (Exception e) {
        throw new DeserializationException("Unable to de-serialize bytes");
      }
      return result;
  }

  // override def isEndOfStream(nextElement: T): Boolean = false
  @Override
    public boolean isEndOfStream(GeneratedMessage nextElement) {
        return false;
    }

  @Override
    public TypeInformation<T> getProducedType() {
        // TODO Auto-generated method stub
        // ????
        // TypeInformation getDescriptorForType
        // return TypeInformationn.of<T>(targetType.get().getClass());
        // lol = targetType.get();
        // Class<? extends T> lol = targetType.get();
        return TypeInformation.of(targetType); // (GeneratedMessage)
        // return TypeInformation<T>(); // targetType.get()
        // return PrimitiveArrayTypeInfo.BYTE_PRIMITIVE_ARRAY_TYPE_INFO;
    }
}