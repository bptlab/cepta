/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bptlab.cepta;

import static org.apache.flink.cep.pattern.Pattern.*;

import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the tutorials and examples on
 * the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run 'mvn clean package' on the
 * command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class StreamingJob {

  public static void main(String[] args) throws Exception {
    // set up the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    /*
     * Here, you can start creating your execution plan for Flink.
     *
     * Start with getting some data from the environment, like
     * 	env.readTextFile(textPath);
     *
     * then, transform the resulting DataStream<String> using operations
     * like
     * 	.filter()
     * 	.flatMap()
     * 	.join()
     * 	.coGroup()
     *
     * and many more.
     * Have a look at the programming guide for the Java API:
     *
     * http://flink.apache.org/docs/latest/apis/streaming/index.html
     *
     */
    // execute program
    env.execute("Start Streaming");
  }

  public static DataStream<Integer> stormFilter(DataStream<Integer> windspeed) {
    return windspeed.filter(
        new FilterFunction<Integer>() {
          @Override
          public boolean filter(Integer speed) throws Exception {
            return speed > 3;
          }
        });
  }

  public static DataStream<String> seaLevelDetector(DataStream<Integer> seaLevels) {

    // this is a pattern of three consecutive(a, b, c) events where a < b < c
    Pattern<Integer, ?> pattern =
        Pattern.<Integer>begin("start")
            .next("middle")
            .where(
                new IterativeCondition<Integer>() {
                  @Override
                  public boolean filter(Integer middle, Context<Integer> context) throws Exception {
                    for (Integer first : context.getEventsForPattern("start")) {
                      if (first > middle) {
                        return false;
                      }
                    }
                    return true;
                  }
                })
            .next("last")
            .where(
                new IterativeCondition<Integer>() {
                  @Override
                  public boolean filter(Integer last, Context<Integer> context) throws Exception {
                    for (Integer middle : context.getEventsForPattern("middle")) {
                      if (middle > last) {
                        return false;
                      }
                    }
                    return true;
                  }
                });
    PatternStream<Integer> patternStream = CEP.pattern(seaLevels, pattern);
    DataStream<String> output =
        patternStream.select(
            new PatternSelectFunction<Integer, String>() {
              @Override
              public String select(Map<String, List<Integer>> map) throws Exception {
                return "Gefahr!";
              }
            });

    return output;
  }
}
