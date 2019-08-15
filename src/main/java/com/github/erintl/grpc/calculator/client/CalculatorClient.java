package com.github.erintl.grpc.calculator.client;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.PrimeNumberDecompositionRequest;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient
{
    public static void main(String[] args)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        // Steaming Service
        Integer number = 567890;
        calculatorClient.primeNumberDecomposition(PrimeNumberDecompositionRequest.newBuilder()
                .setNumber(number)
                .build())
                .forEachRemaining(primeNumberDecompositionResponse -> {
                    System.out.println(primeNumberDecompositionResponse.getPrimeFactor());
                });


        channel.shutdown();
    }

    private static void sum(CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient)
    {
        SumRequest sumRequest = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(25)
                .build();

        SumResponse response = calculatorClient.sum(sumRequest);

        System.out.println(String.format("%d + %d = %d", sumRequest.getFirstNumber(), sumRequest.getSecondNumber(), response.getSumResult()));
    }
}
