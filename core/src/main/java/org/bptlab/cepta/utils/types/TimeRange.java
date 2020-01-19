package org.bptlab.cepta.utils.types;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimeRange {

  private Optional<Timestamp> from = Optional.empty();
  private Optional<Timestamp> to = Optional.empty();

  public TimeRange(Timestamp from, Timestamp to) {
    this.from = Optional.ofNullable(from);
    this.to = Optional.ofNullable(to);
  }

  public TimeRange() {}

  public static TimeRange unconfined() {
    return new TimeRange();
  }

  public String getQuery(String column) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    List<String> query = new ArrayList<String>();
    this.from.map(from -> query.add(String.format("%s >= '%s'", column, dateFormat.format(from))));
    this.to.map(to -> query.add(String.format("%s < '%s'", column, dateFormat.format(to))));
    return query.size() > 0 ? String.format("WHERE %s", String.join(" AND ", query)) : "";
  }

  public Optional<Timestamp> getFrom() {
    return from;
  }

  public void setFrom(Timestamp from) {
    this.from = Optional.ofNullable(from);
  }

  public Optional<Timestamp> getTo() {
    return to;
  }

  public void setTo(Timestamp to) {
    this.to = Optional.ofNullable(to);
  }
}
