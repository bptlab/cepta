package org.bptlab.cepta.pyramid;

import java.util.ArrayList; // import the ArrayList class
import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.pyramid.Level;

public class Level {
    
    private ArrayList<DataStream<?>> dataStreams  = new ArrayList<DataStream<?>>();

    //private DataStream<Event> levelStream;

	public <T> DataStream<T> add(DataStream<T> dataStream) {
            this.dataStreams.add(dataStream);
            return dataStream;
	}

   


    public boolean printLevel(){
        for (int i = 0; i < dataStreams.size(); i++) {
          dataStreams.get(i).print(); 
        }
        return true;
      }
    



  
  


}
