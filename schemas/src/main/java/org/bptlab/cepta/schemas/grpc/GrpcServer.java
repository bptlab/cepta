package org.bptlab.cepta.utils.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GrpcServer<S extends io.grpc.BindableService> {
  private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class.getName());

  private final int port;
  private final Server server;
  private final S service;

  public GrpcServer(Supplier<? extends S> service, int port) {
    this(ServerBuilder.<S>forPort(port), service, port);
  }

  public GrpcServer(S service, int port) {
    this(ServerBuilder.<S>forPort(port), service, port);
  }

  public GrpcServer(ServerBuilder<?> serverBuilder, Supplier<? extends S> service, int port) {
    this(serverBuilder, service.get(), port);
  }

  public GrpcServer(ServerBuilder<?> serverBuilder, S service, int port) {
    this.port = port;
    this.service = service;
    server = serverBuilder.addService(this.service).build();
  }

  public void startGrpcServer() throws IOException {
    server.start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GrpcServer.this.stopGrpcServer();
                System.err.println("*** server shut down");
              }
            });
  }

  public void stopGrpcServer() {
    if (server != null) {
      server.shutdown();
    }
  }

  public void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /*
  public static void main(String[] args) throws Exception {
    // ReplayerServer server = new ReplayerServer(8980);
    this.server.start();
    this.server.blockUntilShutdown();
  }
  */
}
