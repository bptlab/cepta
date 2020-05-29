package org.bptlab.cepta.providers;

import org.bptlab.cepta.models.grpc.replayer.ReplayerGrpc;
import org.bptlab.cepta.models.grpc.replayer.ReplayerGrpc.ReplayerBlockingStub;
import org.bptlab.cepta.models.grpc.replayer.ReplayerGrpc.ReplayerStub;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.QueryOptions;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.ReplayedEvent;
import org.bptlab.cepta.containers.ReplayerContainer;

import io.grpc.StatusRuntimeException;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplayerProvider {

  private final ReplayerBlockingStub blockingStub;
  private final ReplayerStub asyncStub;

  public ReplayerProvider(ReplayerContainer container) {
    this(container.getHost(), container.getPort());
  }

  public ReplayerProvider(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
  }

  public ReplayerProvider(ManagedChannelBuilder<?> channelBuilder) {
    Channel channel = channelBuilder.build();
    this.blockingStub = ReplayerGrpc.newBlockingStub(channel);
    this.asyncStub = ReplayerGrpc.newStub(channel);
  }

  public ArrayList<ReplayedEvent> query(QueryOptions options) {
    Iterator<ReplayedEvent> events;
    ArrayList<ReplayedEvent> eventList = new ArrayList<ReplayedEvent>();
    try {
      events = blockingStub.query(options);
      for (int i = 1; events.hasNext(); i++) {
        ReplayedEvent event = events.next();
        logger.log(Level.INFO, "Result #" + i + ": {0}", event);
        eventList.add(event);
      }
      return eventList;
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return null;
    }
  }
}