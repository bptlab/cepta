package org.bptlab.cepta;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.operators.DataToMongoDB;
import org.bptlab.cepta.operators.LivePlannedCorrelationFunction;
import org.bptlab.cepta.operators.WeatherLocationCorrelationFunction;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.bptlab.cepta.providers.WeatherDataProvider;

import org.testcontainers.containers.GenericContainer;

import com.google.protobuf.GeneratedMessage;

import sun.awt.image.SunWritableRaster.DataStealer;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.*;

public class DataToMongoDBTests {
    public GenericContainer mongo = new GenericContainer("mongo")
        .withExposedPorts(27017)
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example");

    @Test
    public void inputAmount() throws IOException {
         
    }
}
