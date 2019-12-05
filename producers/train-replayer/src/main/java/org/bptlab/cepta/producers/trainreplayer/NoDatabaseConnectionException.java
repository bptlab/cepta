package org.bptlab.cepta.producers.trainreplayer;

class NoDatabaseConnectionException extends Exception {
  NoDatabaseConnectionException(String message) {
    super(message);
  }
}
