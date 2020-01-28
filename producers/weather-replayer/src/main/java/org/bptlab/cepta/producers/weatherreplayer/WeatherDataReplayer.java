package org.bptlab.cepta.producers.weatherreplayer;

import java.sql.ResultSet;
import java.util.Properties;
import org.bptlab.cepta.WeatherData;
import org.bptlab.cepta.producers.PostgresReplayer;
import org.bptlab.cepta.utils.converters.WeatherDataDatabaseConverter;

public class WeatherDataReplayer extends PostgresReplayer<Long, WeatherData> {

  WeatherDataReplayer(Properties props, String topicName) {
    super(props, topicName);
    setSortColumn("ACTUAL_TIME");
    setTableName("public.live");
  }

  @Override
  public WeatherData convertToEvent(ResultSet result) throws Exception {
    return new WeatherDataDatabaseConverter().fromResult(result);
  }
}
