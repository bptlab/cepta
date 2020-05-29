package org.bptlab.cepta.utils.converters;

import com.github.jasync.sql.db.RowData;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public abstract class DatabaseConverter<T> {

  protected  <T extends Date> void convertTimestamp(T ts, Function<Long, ?> resultHandler) {
    Optional.ofNullable(ts).map(T::toInstant).map(
        Instant::toEpochMilli).map(resultHandler);
  }

  protected <T extends LocalDateTime> void convertLocalDateTime(T t, Function<Long, ?> resultHandler) {
    Optional.ofNullable(t).map(T::toDateTime).map(DateTime::getMillis).map(resultHandler);
  }

  protected <T extends LocalDate> void convertLocalDate(T t, Function<Long, ?> resultHandler) {
    Optional.ofNullable(t).map(T::toDate).map(Date::toInstant).map(Instant::toEpochMilli).map(resultHandler);
  }

  public abstract T fromResult(ResultSet result) throws Exception;
  public abstract T fromRowData(RowData result) throws Exception;
}
