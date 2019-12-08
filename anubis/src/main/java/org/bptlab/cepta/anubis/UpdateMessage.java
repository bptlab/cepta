package org.bptlab.cepta.anubis;

public class UpdateMessage {

  private String update;

  public UpdateMessage(String update) {
    this.update = update;
  }

  public String getUpdate() {
    return update;
  }

  public void setUpdate(String name) {
    this.update = update;
  }
}