package com.learn.java.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class SumServer {

  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("Start server");

    Server server = ServerBuilder.forPort(8001)
        .addService(new SumImpl())
        .build();

    server.start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Shutdown server");
      server.shutdown();
      System.out.println("Server is stopped");
    }));

    server.awaitTermination();
  }
}
