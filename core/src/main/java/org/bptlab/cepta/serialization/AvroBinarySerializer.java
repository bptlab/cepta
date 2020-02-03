package org.bptlab.cepta.serialization;

import java.io.IOException;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class AvroBinarySerializer<T extends org.apache.avro.specific.SpecificRecordBase>
    extends AbstractAvroSerializer<T> {
  @Override
  public Encoder getEncoder(T data, ByteArrayOutputStream outputStream) throws IOException {
    return EncoderFactory.get().binaryEncoder(outputStream, null);
  }
}
