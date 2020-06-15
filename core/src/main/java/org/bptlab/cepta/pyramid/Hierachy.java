package org.bptlab.cepta.pyramid;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.IngestionTimeExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import org.bptlab.cepta.pyramid.Transformation;
import java.util.ArrayList; // import the ArrayList class

public class Hierachy {

  public StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    
  private ArrayList<Level> levels = new ArrayList<Level>();

  private ArrayList<View> views = new ArrayList<View>();


  public Level addLevel(Level level){
    this.levels.add(level);
    return level;
  }

  public Level addNewLevel(){
    Level level = new Level();
    this.levels.add(level);
    return level;
  }
  

  public View addView(View view){
    this.views.add(view);
    return view;
  }

  public View addNewView(){
    View view = new View();
    this.views.add(view);
    return view;
  }

    public void drillDown(){}

    

    public void addEventToLevel(){}

    public JobExecutionResult execute(String jobName) throws Exception{
      return env.execute(jobName);
     }
    
}