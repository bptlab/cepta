package org.bptlab.cepta.osiris;

import java.io.*;
import com.fasterxml.jackson.core.*;

public class DataFeedStub {
  private int j;

  public DataFeedStub() {
    this.j = 0;
  }

  public Object nextFeed() throws IOException {
    this.j = this.j + 1;
    Train train = new Train(this.j, "Berlin", "10:00", 30, "Storm", "10:30");

    JsonFactory factory = new JsonFactory();
    StringWriter jsonObjectWriter = new StringWriter();
    JsonGenerator generator = factory.createGenerator(jsonObjectWriter);
    generator.useDefaultPrettyPrinter(); // pretty print JSON

    generator.writeStartObject();
    generator.writeFieldName("errId");
    generator.writeString(Integer.toString(train.getErrId()));
    generator.writeFieldName("station");
    generator.writeString(train.getStation());
    generator.writeFieldName("oldETA");
    generator.writeString(train.getOldETA());
    generator.writeFieldName("delay");
    generator.writeString(Integer.toString(train.getDelay()));
    generator.writeFieldName("cause");
    generator.writeString(train.getCause());
    generator.writeFieldName("newETA");
    generator.writeString(train.getNewETA());
    generator.writeEndObject();

    generator.close(); // to close the generator

    return jsonObjectWriter.toString();
  }
}
