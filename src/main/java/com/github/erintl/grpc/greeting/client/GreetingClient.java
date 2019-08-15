package com.github.erintl.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient
{
    public static void main(String[] args)
    {
        System.out.println("I'm a gRPC client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // Server streaming
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Joe").setLastName("Blow").build())
                .build();
        // Stream responses in a blocking manner.
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private static void unaryGreet(GreetServiceGrpc.GreetServiceBlockingStub greetClient)
    {

        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Joe")
                .setLastName("Blow")
                .build();
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse greetResponse = greetClient.greet(greetRequest);
        System.out.println(greetResponse.getResult());
    }
}
