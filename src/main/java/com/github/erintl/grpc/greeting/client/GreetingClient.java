package com.github.erintl.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient
{
    public static void main(String[] args)
    {
        System.out.println("I'm a gRPC client");

        GreetingClient client = new GreetingClient();
        client.run();
    }

    public void run()
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

//        doUnary(channel);
//        doServerStreaming(channel);
//        doClientStreaming(channel);
//        doBiDiStreamingCall(channel);
        doUnaryWithDeadline(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doServerStreaming(ManagedChannel channel)
    {
        GreetServiceGrpc.GreetServiceBlockingStub stub = GreetServiceGrpc.newBlockingStub(channel);

        // Server streaming
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Joe").setLastName("Blow").build())
                .build();
        // Stream responses in a blocking manner.
        stub.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }

    private void doUnary(ManagedChannel channel)
    {
        GreetServiceGrpc.GreetServiceBlockingStub stub = GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Joe")
                .setLastName("Blow")
                .build();
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse greetResponse = stub.greet(greetRequest);
        System.out.println(greetResponse.getResult());
    }

    private void doUnaryWithDeadline(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub stub = GreetServiceGrpc.newBlockingStub(channel);

        // first call (3000 ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 3000 ms");
            GreetWithDeadlineResponse response = stub.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS)).greetWithDeadline(
                    GreetWithDeadlineRequest.newBuilder().setGreeting(Greeting.newBuilder()
                            .setFirstName("Joe").setLastName("Blow").build())
                            .build());
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }

        // first call (100 ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 100 ms");
            GreetWithDeadlineResponse response = stub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS)).greetWithDeadline(
                    GreetWithDeadlineRequest.newBuilder().setGreeting(Greeting.newBuilder()
                            .setFirstName("Joe").setLastName("Blow").build())
                            .build());
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }
    }

    private void doClientStreaming(ManagedChannel channel)
    {
        GreetServiceGrpc.GreetServiceStub stub = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver =  stub.longGreet(new StreamObserver<>()
        {
            @Override
            public void onNext(LongGreetResponse value)
            {
                // Get a response from the server. Called only once
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t)
            {
                // The server sends an error
            }

            @Override
            public void onCompleted()
            {
                // The server is done sending data. Call immediately following onNext()
                System.out.println("Server has completed sending data");
                latch.countDown();
            }
        });

        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Joe")
                        .setLastName("Blow").build())
                .build());

        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Bill")
                        .setLastName("Toms").build())
                .build());

        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Marc")
                        .setLastName("Johnson").build())
                .build());

        requestObserver.onCompleted();

        try
        {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub stub = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver = stub.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });

        Arrays.asList("Bill", "Ted", "Marc", "Jack").forEach(
                name -> {
                    System.out.println("Sending " + name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName(name)
                                    .setLastName("Smith")
                                    .build())
                            .build());
                }
        );
        requestObserver.onCompleted();

        try
        {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
