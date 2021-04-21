package com.learn.java.client;

import com.learn.grpc.java.proto.AverageRequest;
import com.learn.grpc.java.proto.AverageResponse;
import com.learn.grpc.java.proto.MaximumRequest;
import com.learn.grpc.java.proto.MaximumResponse;
import com.learn.grpc.java.proto.PrimeNumberRequest;
import com.learn.grpc.java.proto.SumRequest;
import com.learn.grpc.java.proto.SumResponse;
import com.learn.grpc.java.proto.SumServiceGrpc;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SumClient {

  public static void main(String[] args) throws InterruptedException {
    System.out.println("run gRPC client");

    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8001)
        .usePlaintext()
        .build();

//    doSum(channel);
    doGetPrimeNumber(channel);
//    doGetAverage(channel);
//    doGetMaximumNumber(channel);

    System.out.println("shutting down channel");
    channel.shutdown();

  }

  private static void doSum(ManagedChannel channel) {
    SumServiceGrpc.SumServiceBlockingStub sumServiceBlockingStub = SumServiceGrpc.newBlockingStub(channel);

    SumRequest request = SumRequest.newBuilder()
        .setNum1(10)
        .setNum2(20)
        .build();
    System.out.println("send request");
    SumResponse response = sumServiceBlockingStub.sum(request);
    System.out.println("response " + response.getResult());

  }

  private static void doGetPrimeNumber(ManagedChannel channel) {
    SumServiceGrpc.SumServiceBlockingStub sumServiceBlockingStub = SumServiceGrpc.newBlockingStub(channel);

    sumServiceBlockingStub
        .withDeadline(Deadline.after(500, TimeUnit.MILLISECONDS)) // timeout
        .primNumber(PrimeNumberRequest.newBuilder()
        .setValue(-1)
        .build())
        .forEachRemaining(primeNumberResponse -> {
          System.out.println(primeNumberResponse.getResult());
        });


  }

  private static void doGetAverage(ManagedChannel channel) throws InterruptedException {
    SumServiceGrpc.SumServiceStub sumServiceStub = SumServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<AverageRequest> requestStreamObserver = sumServiceStub.getAverage(new StreamObserver<AverageResponse>() {
      @Override
      public void onNext(AverageResponse value) {
        System.out.println("receive response from server");
        System.out.println(value.getResult());
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        System.out.println("server has compeleted sending the data");
        latch.countDown();
      }
    });

    for (int i=1;i<5;i++){
      System.out.println("request with value: " + i);
      requestStreamObserver.onNext(AverageRequest.newBuilder()
          .setValue(i)
          .build());
    }

    requestStreamObserver.onCompleted();

    latch.await(120L, TimeUnit.SECONDS);

  }

  private static void doGetMaximumNumber(ManagedChannel channel) throws InterruptedException {
    SumServiceGrpc.SumServiceStub sumServiceStub = SumServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<MaximumRequest> maximumRequestStreamObserver = sumServiceStub.getMaximum(new StreamObserver<MaximumResponse>() {
      @Override
      public void onNext(MaximumResponse value) {
        System.out.println("get response");
        System.out.println(value.getResult());
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        System.out.println("response has completed");
        latch.countDown();
      }
    });

    Arrays.asList(1, 5, 3, 6, 2, 20).forEach(num -> {
      maximumRequestStreamObserver.onNext(MaximumRequest.newBuilder()
          .setValue(num)
          .build());
    });

    maximumRequestStreamObserver.onCompleted();

    latch.await(10L, TimeUnit.SECONDS);

  }
}
