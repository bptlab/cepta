package org.bptlab.cepta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.TrainDelayNotificationOuterClass.TrainDelayNotification;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.DelayShiftFunctionMongo;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.bptlab.cepta.providers.WeatherDataProvider;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.junit.Assert;
import org.junit.Test;
import java.sql.*;

public class DelayShiftFunctionMongoTests {

   @Test
   public void testRightAmount() throws IOException {

   }

   @Test
   public void testDelayNotificationGeneration() throws IOException {

   }

   @Test
   public void testDateConsideration() throws IOException {

   }

}
