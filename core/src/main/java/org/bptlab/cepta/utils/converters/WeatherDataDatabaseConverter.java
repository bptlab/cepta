package org.bptlab.cepta.utils.converters;

import com.github.jasync.sql.db.RowData;
import java.sql.ResultSet;
import org.bptlab.cepta.models.events.weather.WeatherDataProtos.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherDataDatabaseConverter extends DatabaseConverter<WeatherData> {
  private static final Logger logger = LoggerFactory.getLogger(WeatherDataDatabaseConverter.class.getName());

  public WeatherData fromResult(ResultSet result) throws Exception {
    WeatherData.Builder event = WeatherData.newBuilder();
    try {
      event.setEventclass(result.getString("class"));
      event.setLatitude(result.getDouble("latitude"));
      event.setLongitude(result.getDouble("longitude"));
      convertTimestamp(result.getTimestamp("starttimestamp"), event::setStarttimestamp);
      convertTimestamp(result.getTimestamp("endtimestamp"), event::setEndtimestamp);
      convertTimestamp(result.getTimestamp("detectiontimestamp"), event::setDetectiontimestamp);
      event.setTitle(result.getString("title"));
      event.setDescription(result.getString("description"));
      event.setTemperature(result.getDouble("temperature"));
      event.setRain(result.getDouble("rain"));
      event.setWindspeed(result.getDouble("windspeed"));
      event.setCloudpercentage(result.getDouble("cloudpercentage"));
      event.setCityname(result.getString("cityname"));
      event.setIdentifier(result.getString("identifier"));
      event.setPressure(result.getDouble("pressure"));
      event.setOzone(result.getDouble("ozone"));
      event.setHumidity(result.getDouble("humidity"));
      event.setWindbearing(result.getInt("windbearing"));
      event.setPrecippropability(result.getDouble("precippropability"));
      event.setPreciptype(result.getString("preciptype"));
      event.setDewpoint(result.getDouble("dewpoint"));
      event.setNeareststormbearing(result.getInt("neareststormbearing"));
      event.setNeareststormdistance(result.getInt("neareststormdistance"));
      event.setVisibility(result.getDouble("visibility"));
    } catch (Exception e) {
      logger.error("Failed to convert database entry to live train data event");
      throw e;
    }
    logger.debug(event.build().toString());
    return event.build();
  }

  @Override
  public WeatherData fromRowData(RowData result) throws Exception {
    WeatherData.Builder event = WeatherData.newBuilder();
    try {
      event.setEventclass(result.getString("class"));
      event.setLatitude(result.getDouble("latitude"));
      event.setLongitude(result.getDouble("longitude"));
      convertLocalDateTime(result.getDate("starttimestamp"), event::setStarttimestamp);
      convertLocalDateTime(result.getDate("endtimestamp"), event::setEndtimestamp);
      convertLocalDateTime(result.getDate("detectiontimestamp"), event::setDetectiontimestamp);
      event.setTitle(result.getString("title"));
      event.setDescription(result.getString("description"));
      event.setTemperature(result.getDouble("temperature"));
      event.setRain(result.getDouble("rain"));
      event.setWindspeed(result.getDouble("windspeed"));
      event.setCloudpercentage(result.getDouble("cloudpercentage"));
      event.setCityname(result.getString("cityname"));
      event.setIdentifier(result.getString("identifier"));
      event.setPressure(result.getDouble("pressure"));
      event.setOzone(result.getDouble("ozone"));
      event.setHumidity(result.getDouble("humidity"));
      event.setWindbearing(result.getInt("windbearing"));
      event.setPrecippropability(result.getDouble("precippropability"));
      event.setPreciptype(result.getString("preciptype"));
      event.setDewpoint(result.getDouble("dewpoint"));
      event.setNeareststormbearing(result.getInt("neareststormbearing"));
      event.setNeareststormdistance(result.getInt("neareststormdistance"));
      event.setVisibility(result.getDouble("visibility"));
    } catch (Exception e) {
      logger.error("Failed to convert database entry to live train data event");
      throw e;
    }
    logger.debug(event.build().toString());
    return event.build();
  }

}
