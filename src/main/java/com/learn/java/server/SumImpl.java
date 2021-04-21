package com.learn.java.server;

import com.learn.grpc.java.proto.AverageRequest;
import com.learn.grpc.java.proto.AverageResponse;
import com.learn.grpc.java.proto.MaximumRequest;
import com.learn.grpc.java.proto.MaximumResponse;
import com.learn.grpc.java.proto.PrimeNumberRequest;
import com.learn.grpc.java.proto.PrimeNumberResponse;
import com.learn.grpc.java.proto.SumRequest;
import com.learn.grpc.java.proto.SumResponse;
import com.learn.grpc.java.proto.SumServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class SumImpl extends SumServiceGrpc.SumServiceImplBase {

  @Override
  public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
    responseObserver.onNext(SumResponse.newBuilder()
        .setResult(request.getNum1() + request.getNum2())
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void primNumber(PrimeNumberRequest request, StreamObserver<PrimeNumberResponse> responseObserver) {

    int num = request.getValue();
    if(num < 0) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("number shouldn't under 0")
              .augmentDescription("request number: " + num)
              .asRuntimeException()
      );
    }

    int k = 2;
    while (num > 1) {
      if(num % k == 0) {
        responseObserver.onNext(PrimeNumberResponse.newBuilder()
            .setResult(k)
            .build());
        num = num / k;
      } else {
        k += 1;
      }
    }

    responseObserver.onCompleted();
  }

  @Override
  public StreamObserver<AverageRequest> getAverage(StreamObserver<AverageResponse> responseObserver) {
    return new StreamObserver<AverageRequest>() {

      double count = 0;
      double total = 0;

      @Override
      public void onNext(AverageRequest value) {
        System.out.println("get request value: " + value);
        count+=1;
        total += value.getValue();
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        double average = total/count;

        System.out.println("request completed average: " + average);

        responseObserver.onNext(AverageResponse.newBuilder()
            .setResult(average)
            .build());
        responseObserver.onCompleted();

      }
    };


  }

  @Override
  public StreamObserver<MaximumRequest> getMaximum(StreamObserver<MaximumResponse> responseObserver) {

    return new StreamObserver<MaximumRequest>() {

      int maxNumber = 0;

      @Override
      public void onNext(MaximumRequest value) {
        if(value.getValue() > maxNumber) {
          maxNumber = value.getValue();
          responseObserver.onNext(MaximumResponse.newBuilder()
              .setResult(maxNumber)
              .build());
        }
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        responseObserver.onCompleted();
      }
    };
  }
}
