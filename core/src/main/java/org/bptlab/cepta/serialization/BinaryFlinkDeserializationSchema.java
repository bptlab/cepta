package org.bptlab.cepta.serialization;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import java.util.function.Supplier;
import com.google.protobuf.Parser;
import org.apache.flink.api.common.typeinfo.PrimitiveArrayTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.types.DeserializationException;

/*
public class BinaryFlinkDeserializationSchema<T extends SpecificRecordBase> implements 
DeserializationSchema<T> {
  
  @Override
  public T deserialize(byte[] message) {
    return new AvroBinarySerializer<T>().serialize("", o);
    // T addressBook =
    // T.parseFrom(new FileInputStream(args[0]));
  }

}
*/
/*
public class BinaryFlinkDeserializationSchema {

}
*/

// /*
// with ](parser: Array[Byte] => T)
public class BinaryFlinkDeserializationSchema<T extends GeneratedMessage> implements DeserializationSchema<T> {

    // Supplier<? extends T> targetType;
    Supplier<? extends T> targetType;

  // protected final Class<T> targetType;
  public BinaryFlinkDeserializationSchema(Supplier<? extends T> targetType) {
    this.targetType = targetType;
  }

    @Override
  public T deserialize(byte[] message) {
    T result = null;
      try {
          // return Parser<T>()->parseFrom(message);
          result = (T) targetType.get().getParserForType().parseFrom(message);
        // return T.parser().parseWithIOException(message); //.getParserForType().parseFrom(message);
      } catch (Exception e) {
        throw new DeserializationException("Unable to de-serialize bytes");
      }
      return result;
  }

  // override def isEndOfStream(nextElement: T): Boolean = false
  @Override
    public boolean isEndOfStream(T nextElement) {
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
        // return TypeInformation.of(lol);
        // return TypeInformation<T>(); // targetType.get()
        // return PrimitiveArrayTypeInfo.BYTE_PRIMITIVE_ARRAY_TYPE_INFO;
    }
}
// */

/*
public class AvroBinaryDeserializer<T extends org.apache.avro.specific.SpecificRecordBase>
    implements org.apache.kafka.common.serialization.Deserializer<T> {

  
  public AvroBinaryDeserializer(Class<T> targetType) {
    this.targetType = targetType;
  }

  public AvroBinaryDeserializer(Supplier<? extends T> targetType) {
    this.targetType = targetType;
  }

  @Override
  public void configure(Map<String, ?> map, boolean b) {}

  @SuppressWarnings("unchecked")
  @Override
  public T deserialize(String topic, byte[] data) {
    T result = null;
    try {
      if (data != null) {
        DatumReader<GenericRecord> datumReader =
            new SpecificDatumReader<>(targetType.get().getSchema());
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        result = (T) datumReader.read(null, decoder);
      }
    } catch (Exception e) {
      throw new SerializationException(
          "Can't deserialize data='" + Arrays.toString(data) + "' using generic avro deserializer");
    }
    return result;
  }

  @Override
  public void close() {}
}
*/