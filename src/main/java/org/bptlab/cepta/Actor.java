package org.bptlab.cepta;

public class Actor{
  public int id;
  public String name;
  public Actor(){

  }
  public Actor(int id, String name){
    this.id = id;
    this.name = name;
  }
  public String columnString(){
    return "(id, name)";
  }
}
