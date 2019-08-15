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
}
