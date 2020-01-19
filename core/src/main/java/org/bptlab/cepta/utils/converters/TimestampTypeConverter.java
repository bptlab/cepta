package org.bptlab.cepta.utils.converters;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import picocli.CommandLine.ITypeConverter;

public class TimestampTypeConverter implements ITypeConverter<Optional<Timestamp>> {
  public Optional<Timestamp> convert(String value) throws Exception {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Date parsedDate = dateFormat.parse(value);
    return Optional.of(new Timestamp(parsedDate.getTime()));
  }
}
