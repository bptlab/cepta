package org.bptlab.cepta.osiris;

class Train{
  private int errId;
  private String station;
  private String oldETA;
  private int delay;
  private String cause;
  private String newETA;

  public Train(int errId, String station, String oldETA, int delay, String cause, String newETA){
    this.errId = errId;
    this.station = station;
    this.oldETA = oldETA;
    this. delay = delay;
    this.cause = cause;
    this.newETA = newETA;
  }

  public int getErrId() {
    return this.errId;
  }

  public String getStation() {
    return this.station;
  }
  public String getOldETA() {
    return this.oldETA;
  }
  public int getDelay() {
    return this.delay;
  }
  public String getCause() {
    return this.cause;
  }
  public String getNewETA() {
    return this.newETA;
  }

}