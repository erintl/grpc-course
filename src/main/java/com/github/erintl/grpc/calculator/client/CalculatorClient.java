package com.github.erintl.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient
{
    public static void main(String[] args)
    {
        CalculatorClient client = new CalculatorClient();
        client.run();
    }

    private static void primeNumberDecomposition(ManagedChannel channel)
    {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        // Steaming Service
        int number = 567890;
        calculatorClient.primeNumberDecomposition(PrimeNumberDecompositionRequest.newBuilder()
                .setNumber(number)
                .build())
                .forEachRemaining(primeNumberDecompositionResponse -> {
                    System.out.println(primeNumberDecompositionResponse.getPrimeFactor());
                });
    }

    public void run()
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        average(channel);

        channel.shutdown();
    }

    private void sum(ManagedChannel channel)
    {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest sumRequest = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(25)
                .build();

        SumResponse response = calculatorClient.sum(sumRequest);

        System.out.println(String.format("%d + %d = %d", sumRequest.getFirstNumber(), sumRequest.getSecondNumber(), response.getSumResult()));
    }

    private void average(ManagedChannel channel)
    {
        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<ComputeAverageRequest> requestObserver = stub.computeAverage(new StreamObserver<ComputeAverageResponse>()
        {
            @Override
            public void onNext(ComputeAverageResponse value)
            {
                System.out.println("Received response from server");
                System.out.println(value.getAverage());
            }

            @Override
            public void onError(Throwable t)
            {

            }

            @Override
            public void onCompleted()
            {
                System.out.println("Sever has completed sending data");
            }
        });

        for (int i = 0; i < 10000; i++)
        {
            requestObserver.onNext(ComputeAverageRequest.newBuilder()
                    .setNumber(i)
                    .build());
        }

        requestObserver.onCompleted();
        try
        {
            latch.await(3, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
