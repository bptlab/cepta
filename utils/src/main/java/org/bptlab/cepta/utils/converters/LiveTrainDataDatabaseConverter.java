package org.bptlab.cepta.utils.converters;

import com.github.jasync.sql.db.RowData;
import java.sql.ResultSet;
import org.bptlab.cepta.LiveTrainData;

public class LiveTrainDataDatabaseConverter extends DatabaseConverter<LiveTrainData> {
  public LiveTrainData fromResult(ResultSet result) throws Exception {
    return null;
  }

  public LiveTrainData fromRowData(RowData result) throws Exception {
    return null;
  }
}
