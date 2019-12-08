package org.bptlab.cepta.schemas.grpc;

import org.bptlab.cepta.producers.replayer.Empty;
import org.bptlab.cepta.producers.replayer.ReplayerGrpc;
import org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerBlockingStub;
import org.bptlab.cepta.producers.replayer.ReplayerGrpc.ReplayerStub;
import org.bptlab.cepta.producers.replayer.Success;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;

public class ReplayerClient {

  private final ManagedChannel channel;
  private final ReplayerBlockingStub blockingStub;
  private final ReplayerStub asyncStub;

  public ReplayerClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
  }

  private ReplayerClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = ReplayerGrpc.newBlockingStub(channel);
    asyncStub = ReplayerGrpc.newStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public Success start() throws InterruptedException {
    return blockingStub.start(Empty.newBuilder().build());
  }

}