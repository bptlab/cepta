package org.bptlab.cepta.providers;

import org.bptlab.cepta.models.grpc.replayer.ReplayerGrpc;
import org.bptlab.cepta.models.grpc.replayer.ReplayerGrpc.ReplayerBlockingStub;
import org.bptlab.cepta.models.grpc.replayer.ReplayerGrpc.ReplayerStub;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.QueryOptions;
import org.bptlab.cepta.models.grpc.replayer.ReplayerOuterClass.ReplayedEvent;
import org.bptlab.cepta.containers.ReplayerContainer;

import io.grpc.StatusRuntimeException;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Iterator;

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

  public Iterator<ReplayedEvent> query(QueryOptions options) throws StatusRuntimeException {
    /*
    ReplayedEvent request =
        Rectangle.newBuilder()
            .setLo(Point.newBuilder().setLatitude(lowLat).setLongitude(lowLon).build())
            .setHi(Point.newBuilder().setLatitude(hiLat).setLongitude(hiLon).build()).build();
     */
    // return blockingStub.query(options);


    Iterator<ReplayedEvent>


    /*
    try {
      features = blockingStub.listFeatures(request);
    } catch (StatusRuntimeException ex) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    */
  }

}