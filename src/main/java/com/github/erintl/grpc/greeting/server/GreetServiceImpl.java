package com.github.erintl.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase
{
    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver)
    {
        // extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();
        String lastName = greeting.getLastName();

        // create the response
        String result = String.format("Hello %s %s", firstName, lastName);
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        // send the response
        responseObserver.onNext(response);

        // complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver)
    {
        String firstName = request.getGreeting().getFirstName();
        String lastName = request.getGreeting().getLastName();

        try
        {
            for (int i = 0; i < 10; i++)
            {
                String result = String.format("Hello %s %s, response number: %d", firstName, lastName, i);
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                        .setResult(result)
                        .build();
                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver)
    {
        return new StreamObserver<>()
        {
            String result = "";

            @Override
            public void onNext(LongGreetRequest value)
            {
                // Client sends a message
                result += "Hello " + value.getGreeting().getFirstName() + "-" + value.getGreeting().getLastName() + "! ";
            }

            @Override
            public void onError(Throwable t)
            {
                // Client sends an error
            }

            @Override
            public void onCompleted()
            {
                // Client is done, this is when a response is to be returned (responseObserver)
                responseObserver.onNext(LongGreetResponse.newBuilder().setResult(result).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {
                String response = "Hello " + value.getGreeting().getFirstName() + " " + value.getGreeting().getLastName();
                GreetEveryoneResponse greetEveryoneResponse = GreetEveryoneResponse.newBuilder()
                        .setResult(response)
                        .build();
                responseObserver.onNext(greetEveryoneResponse);
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
