package org.bptlab.cepta.pyramid;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
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

    //public void drillDown(){}

    public JobExecutionResult execute(String jobName) throws Exception{
      return env.execute(jobName);
     }
    
}