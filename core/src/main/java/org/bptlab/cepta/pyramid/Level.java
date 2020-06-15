package org.bptlab.cepta.pyramid;

import java.util.ArrayList; // import the ArrayList class

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
import org.bptlab.cepta.pyramid.Level;
import org.bptlab.cepta.pyramid.Transformation;
import java.util.ArrayList; // import the ArrayList class

public class Level {
    public int id = 1;
    
    private ArrayList<DataStream<?>> dataStreams  = new ArrayList<DataStream<?>>();


	public <T> DataStream<T> add(DataStream<T> dataStream) {
            this.dataStreams.add(dataStream);
            return dataStream;
	}

    //public ArrayList<?> transformations;


    public boolean printLevel(){
        for (int i = 0; i < dataStreams.size(); i++) {
          dataStreams.get(i).print(); 
        }
        return true;
      }
    



   /*  public ArrayList<DataStream<?>> events = new ArrayList<DataStream<?>>();

    public void addEvents(DataStream<?> event){
        events.add(event);
    } */
    
  


}