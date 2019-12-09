package org.bptlab.cepta.utils.converters;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

public abstract class DatabaseConverter<T> {

  protected  <T extends Date> void convertTimestamp(T ts, Function<Long, ?> resultHandler) {
    Optional.ofNullable(ts).map(T::toInstant).map(
        Instant::toEpochMilli).map(resultHandler);
  }

  public abstract T fromResult(ResultSet result) throws Exception;
}
