package org.bptlab.cepta.pyramid;

import java.util.ArrayList;

import org.apache.flink.streaming.api.datastream.DataStream;


public class View {
    
    private ArrayList<Level> levels = new ArrayList<Level>();

    private ArrayList<DataStream<?>> dataStreams  = new ArrayList<DataStream<?>>();

    //private  DataStream<?> viewStream; 

    public <T> DataStream<T> add(DataStream<T> dataStream) {
        this.dataStreams.add(dataStream);
        return dataStream;
    }

    public Level add(Level level){
        this.levels.add(level);
        return level;
    }



    public boolean printView(){
        for (int i = 0; i < dataStreams.size(); i++) {
            dataStreams.get(i).print(); 
          }
        for (int i = 0; i < levels.size(); i++) {
        levels.get(i).printLevel(); 
        }

        return true;
      }
}